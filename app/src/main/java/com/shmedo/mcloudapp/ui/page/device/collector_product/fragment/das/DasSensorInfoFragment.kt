package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTRainStation
import com.shmedo.lib.cmd.base.iot_cmd.enums.SensorErrorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasSubSensorStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDRainStation
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoThree
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.cmd.base.md_cmd.model.das.InclinometerInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 物联网采集器(DAS)传感器信息
 *
 */
class DasSensorInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private var isBleMode = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "传感器信息"
    }

    override fun initData() {
        super.initData()
        // 判断通讯方式
        isBleMode = communicateWay == BleConnect
    }

    override fun queryStatusInfo() {
        if (isBleMode) {
            queryBleInfo()
        } else {
            query4GInfo()
        }
    }

    /**
     * 4G通讯模式查询信息
     */
    private fun query4GInfo() {
        commandItems.clear()

        var command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SENSOR_STATUS, "index=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 蓝牙通讯模式查询信息
     */
    private fun queryBleInfo() {
        commandItems.clear()

        /**
         * 查询设备状态2:##042\r\n<br/>
         * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n<br/>
         * （1）SN号<br/>
         * （2）经度<br/>
         * （3）纬度<br/>
         * （4）设备内部电压<br/>
         * （5）设备外部电压<br/>
         * （6）太阳能控制器状态<br/>
         * （7）太阳能板电压<br/>
         * （8）电池电压<br/>
         * （9）日发电量<br/>
         * （10）日耗电量<br/>
         * （11）机箱内部温湿度状态<br/>
         * （12）机箱内部温度<br/>
         * （13）机箱内部湿度<br/>
         * （14）机箱外部温湿度状态<br/>
         * （15）机箱外部温度<br/>
         * （16）机箱外部湿度<br/>
         * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器<br/>
         * （18）降雨量或断线报警器状态(1:断开，0：闭合)<br/>
         * 示例：$$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0<br/>
         */
        var command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_2)
        commandItems.add(command)

        //查询倾角计信息
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_INCLINOMETER_INFO)
        commandItems.add(command)

        /**
         * 获取主传感器状态:##043\r\n<br/>
         * 应答:$$043,(1),(2),(3),(4),(5)<br/>
         * （1）SN号<br/>
         * （2）采集器型号<br/>
         * （3）采集器地址(当采集器地址为0时，关闭采集功能)<br/>
         * （4）传感器状态，用冒号分隔的字符串<br/>
         * ①:②:③，其中 ①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据<br/>
         * （5）传感器状态，和（2）格式相同，<br/>
         * 注：传感器状态可能有很多个，有接入传感器个数决定。<br/>
         */
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_3)
        commandItems.add(command)


        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (isBleMode) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理4G通讯命令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询辅传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GInternalSensorData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询扩展传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GExternalSensorData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /**
     * 处理蓝牙通讯命令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_2 -> {//##042\r\n：查询设备状态2
                val result = mdParseManager.parse<DeviceStatusInfoTwo>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_2
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleDeviceStatus(result.data)
                    }
                }
            }

            MDCommandType.QUERY_INCLINOMETER_INFO -> {//##046
                val result = mdParseManager.parse<InclinometerInfo>(
                    cmdStr,
                    MDCommandType.QUERY_INCLINOMETER_INFO
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询倾角计信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleInclinometerInfo(result.data)
                    }
                }
            }

            MDCommandType.QUERY_DAS_STATUS_3 -> {//##043\r\n: 获取主传感器状态
                val result = mdParseManager.parse<DeviceStatusInfoThree>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_3
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询扩展传感器状态出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleExternalSensorData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun init4GInternalSensorData(content: String) {
        launchWithViewLifecycle {
            try {
                if (content.isEmpty() || content == "{}") {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val subSensorStatusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<DasSubSensorStatusInfo>(content)
                } ?: return@launchWithViewLifecycle

                val groupList = mutableListOf<Any>()

                //开关量传感器
                subSensorStatusInfo.io?.let { ioBean ->
                    if (ioBean.type.isNotEmpty()) {
                        groupList.add(DeviceStatusInfoGroupItem("开关量传感器"))
                        when (IOTRainStation.value(ioBean.type)) {
                            IOTRainStation.CLOSE -> {//关闭
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "未接入",
                                        isBottomItem = true
                                    )
                                )
                            }

                            IOTRainStation.RAIN_OPEN -> {//雨量计
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "已接入",
                                        ColorUtils.getColor(R.color.online_colorPrimary)
                                    )
                                )
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "雨量值（毫米）",
                                        value = ioBean.vaule,
                                        isBottomItem = true
                                    )
                                )
                            }

                            IOTRainStation.ALARM_OPEN -> {//断线报警器
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "已接入",
                                        ColorUtils.getColor(R.color.online_colorPrimary)
                                    )
                                )
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "断线报警器",
                                        value = if (ioBean.vaule == "1") "已断线" else "未断线",
                                        textColorRes = if (ioBean.vaule == "1") ColorUtils.getColor(
                                            R.color.online_colorPrimary
                                        ) else 0,
                                        isBottomItem = true
                                    )
                                )
                            }
                        }
                    }
                }

                //数字水位计
                subSensorStatusInfo.vwp?.let { vwpBean ->
                    if (vwpBean.errno.isNotEmpty()) {
                        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                        groupList.add(DeviceStatusInfoGroupItem("数字水位计"))
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "状态",
                                value = if (vwpBean.errno == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                                    vwpBean.errno
                                ),
                                textColorRes = if (vwpBean.errno == "0") 0 else ColorUtils.getColor(
                                    R.color.error_FF4400
                                )
                            )
                        )

                        val dataList =
                            vwpBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        if (dataList.isNotEmpty()) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "水深（米）",
                                    value = dataList[0],
                                    isBottomItem = dataList.size == 1
                                )
                            )
                        }
                        if (dataList.size >= 2) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "空管距离（米）",
                                    value = dataList[1],
                                    isBottomItem = dataList.size == 2
                                )
                            )
                        }
                        if (dataList.size >= 3) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "水温（摄氏度）",
                                    value = dataList[2],
                                    isBottomItem = dataList.size == 3
                                )
                            )
                        }
                    }
                }

                //倾角计
                subSensorStatusInfo.mems?.let { memsBean ->
                    if (memsBean.errno.isNotEmpty()) {
                        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                        groupList.add(DeviceStatusInfoGroupItem("倾角计"))
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "状态",
                                value = if (memsBean.errno == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                                    memsBean.errno
                                ),
                                textColorRes = if (memsBean.errno == "0") 0 else ColorUtils.getColor(
                                    R.color.error_FF4400
                                )
                            )
                        )
                        val dataList =
                            memsBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        if (dataList.isNotEmpty()) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "X轴角度（度）",
                                    value = dataList[0],
                                    isBottomItem = dataList.size == 1
                                )
                            )
                        }
                        if (dataList.size >= 2) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Y轴角度（度）",
                                    value = dataList[1],
                                    isBottomItem = dataList.size == 2
                                )
                            )
                        }
                        if (dataList.size >= 3) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Z轴角度（度）",
                                    value = dataList[2],
                                    isBottomItem = dataList.size == 3
                                )
                            )
                        }
                        if (dataList.size >= 4) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "X轴加速度(mg)",
                                    value = dataList[3],
                                    isBottomItem = dataList.size == 4
                                )
                            )
                        }
                        if (dataList.size >= 5) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Y轴加速度(mg)",
                                    value = dataList[4],
                                    isBottomItem = dataList.size == 5
                                )
                            )
                        }
                        if (dataList.size >= 6) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Z轴加速度(mg)",
                                    value = dataList[5],
                                    isBottomItem = dataList.size == 6
                                )
                            )
                        }
                    }
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun init4GExternalSensorData(content: String) {
        launchWithViewLifecycle {
            try {
                val dataList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<DasSensorStatusInfo>>(content)
                }
                if (dataList.isNullOrEmpty()) {
                    if (binding.recyclerview.models?.isEmpty() == true)
                        binding.refreshLayout.showEmpty()

                    return@launchWithViewLifecycle
                }

                val groupList = mutableListOf<Any>()
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("扩展传感器"))
                dataList.forEach { sensorInfo ->
                    sensorInfo.valueList =
                        sensorInfo._val.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    groupList.add(sensorInfo)
//                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                }

                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initBleDeviceStatus(info: DeviceStatusInfoTwo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("开关量传感器"))
            when (MDRainStation.value(info.switchType)) {
                MDRainStation.CLOSE -> {//2：关闭
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "未接入",
                            isBottomItem = true
                        )
                    )
                }

                MDRainStation.RAIN_OPEN -> {//1：雨量计
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "已接入",
                            ColorUtils.getColor(R.color.online_colorPrimary)
                        )
                    )
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "雨量值（毫米）",
                            value = info.rainfallStatus,
                            isBottomItem = true
                        )
                    )
                }

                MDRainStation.ALARM_OPEN -> {//3：断线报警器
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "已接入",
                            ColorUtils.getColor(R.color.online_colorPrimary)
                        )
                    )
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "断线报警器",
                            value = if (info.rainfallStatus == "1" || info.rainfallStatus == "1.0") "断线" else "未断线",
                            textColorRes = if (info.rainfallStatus == "1" || info.rainfallStatus == "1.0") ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ) else 0,
                            isBottomItem = true
                        )
                    )
                }
            }

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 设置倾角计信息
     */
    private fun initBleInclinometerInfo(info: InclinometerInfo) {
        try {
            if (info.status == "2")
                return

            val groupList = mutableListOf<Any>()
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("倾角计"))
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "状态",
                    value = if (info.status == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                        info.status
                    ),
                    textColorRes = if (info.status == "0") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "X轴角度（度）",
                value = info.xAxis,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Y轴角度（度）",
                value = info.yAxis,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Z轴角度（度）",
                value = info.zAxis,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "X轴加速度(mg)",
                value = info.xAcceleration,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Y轴加速度(mg)",
                value = info.yAcceleration,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Z轴加速度(mg)",
                value = info.zAcceleration,
                isBottomItem = true
            )

            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBleExternalSensorData(info: DeviceStatusInfoThree) {
        try {
            if (info.collectorAddress == "0" || info.sensorStatus.isNullOrEmpty()) {
                if (binding.recyclerview.models?.isEmpty() == true)
                    binding.refreshLayout.showEmpty()
                return
            }

            val groupList = mutableListOf<Any>()
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("扩展传感器"))
            for (i in 0 until info.sensorStatus.size) {
                val tempStr = info.sensorStatus[i]
                //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，2 不展示 ③：传感器数据
                val tempList = tempStr.split(":").dropLastWhile { it.isEmpty() }
                if (tempList.size < 3 || tempList[1] == "2") {
                    continue
                }
                groupList.add(
                    DasSensorStatusInfo(
                        type = info.collectorModel,
                        addr = tempList[0].toIntOrNull() ?: 0,
                        errno = tempList[1].toIntOrNull() ?: 0,
                        valueList = tempList.subList(2, tempList.size)
                    )
                )
//                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            }

            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

}