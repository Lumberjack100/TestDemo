package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasTemperatureAndHumidityStatusinfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoOne
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.cmd.base.md_cmd.model.das.SystemRunStateInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.VersionMessageInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 物联网采集器(DAS)基本信息 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasBaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private var isBleMode = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 蓝牙通讯模式查询信息
     */
    private fun queryBleInfo() {
        commandItems.clear()

        /**
         * 查询设备状态1: ##041\r\n <br/>
         * 应答: $$041,(1),(2),(3),(4),(5),(6)\r\n <br/>
         * （1）SN号 <br/>
         * （2）IMEI号 <br/>
         * （3）SIM卡号 <br/>
         * （4）启动代码1 <br/>
         * （5）启动代码2 <br/>
         * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号 <br/>
         */
        var command = MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_1)
        commandItems.add(command)

        /**
         * 获取版本信息 ##040\r\n <br/>
         * 应答: $$040,(1),(2),(3)\r\n<br/>
         * (1)“产品序列号”<br/>
         * (2)“固件版本号”<br/>
         * (3)“生产日期”<br/>
         */
        command = MDCommandUtil.getCommand(MDCommandType.VERSION_MESSAGE)
        commandItems.add(command)

        /**
         * 获取信号强度 ##014\r\n<br/>
         * 应答:$$014,(1),(2) ,(3),(4) ,(5),(6) ,(7),(8), (9),(10) \r\n<br/>
         * (1)：信号值<br/>
         * (2)：GPS定位搜星数目<br/>
         * (3)：启动代码<br/>
         * (4)：重启代码<br/>
         * (5)：sim卡ccid<br/>
         * (6)：设备内部温度<br/>
         * (7)：设备内部电池电压<br/>
         * (8)：设备外部电压<br/>
         * (9)：运营商类型<br/>
         * (10)：网络制式<br/>
         */
        command = MDCommandUtil.getCommand(MDCommandType.SYSTEM_RUN_STATE)
        commandItems.add(command)

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
        command = MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_2)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun isTargetCommandType(commandType: IOTCommandType): Boolean = true

    override fun setResultData(cmdStr: String) {
        if (isBleMode) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理4G通讯指令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DEVICE_BASE -> {
                val result = iotParseManager.parse<DasBaseInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DEVICE_BASE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询基本信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GBaseInfo(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询温湿度状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GTemperatureAndHumidityStatus(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun init4GBaseInfo(baseInfo: DasBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("设备信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = baseInfo.sn,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备启动代码",
                value = baseInfo.code,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = baseInfo.ver,
                isBottomItem = true
            )

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("供电信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "外部电压",
                value = baseInfo.outvolt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                unit = "V"
            )
            val batteryCapacity =
                baseInfo.involt.replace("%", "").toDoubleOrNull() ?: Double.MAX_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "内部电量",
                value = baseInfo.involt.replace("%", "").ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                unit = "%",
                textColorRes = if (batteryCapacity > 25) 0 else ColorUtils.getColor(
                    R.color.warn_FF9D00
                ),
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 温湿度状态
     */
    private fun init4GTemperatureAndHumidityStatus(content: String) {
        launchWithViewLifecycle {
            try {
                if (content.isEmpty() || content == "{}") {
                    return@launchWithViewLifecycle
                }
                val info = MoshiUtil.fromJson<DasTemperatureAndHumidityStatusinfo>(content)
                    ?: return@launchWithViewLifecycle

                val groupList = mutableListOf<Any>()
                if (info.inth.errno.isNotEmpty() && info.inth.errno != "0") {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("环境信息"))

                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "内部温度",
                        value = info.inth.temp,
                        defaultValue = AppContants.PLACE_HOLDER_VALUE,
                        digit = 2,
                        unit = "℃",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "内部湿度",
                        value = info.inth.humi,
                        defaultValue = AppContants.PLACE_HOLDER_VALUE,
                        digit = 2,
                        unit = "%",
                        isBottomItem = info.outth.errno.isEmpty()
                    )
                }

                if (info.outth.errno.isNotEmpty() && info.inth.errno != "0") {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "外部温度",
                        value = info.outth.temp,
                        defaultValue = AppContants.PLACE_HOLDER_VALUE,
                        digit = 2,
                        unit = "℃",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "外部湿度",
                        value = info.outth.humi,
                        defaultValue = AppContants.PLACE_HOLDER_VALUE,
                        digit = 2,
                        unit = "%",
                        isBottomItem = true
                    )
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

    /**
     * 处理蓝牙通讯指令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_1 -> {//##041\r\n：查询设备状态1
                val result = mdParseManager.parse<DeviceStatusInfoOne>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_1
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基本信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleDeviceStatus1(result.data)
                    }
                }
            }

            MDCommandType.VERSION_MESSAGE -> {//##040\r\n：获取版本信息
                val result = mdParseManager.parse<VersionMessageInfo>(
                    cmdStr,
                    MDCommandType.VERSION_MESSAGE
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询版本信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleDeviceStatus2(result.data)
                    }
                }
            }

            MDCommandType.SYSTEM_RUN_STATE -> {//##014\r\n：获取信号强度
                val result = mdParseManager.parse<SystemRunStateInfo>(
                    cmdStr,
                    MDCommandType.SYSTEM_RUN_STATE
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询运行状态出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleDeviceStatus3(result.data)
                    }
                }
            }

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
                        initBleDeviceStatus4(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBleDeviceStatus1(info: DeviceStatusInfoOne) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("设备信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = info.snNumber,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备启动代码",
                value = "${MDCommandUtil.formatStringTwo(info.startCodeOne)}${
                    MDCommandUtil.formatStringTwo(info.startCodeTwo)
                }",
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBleDeviceStatus2(info: VersionMessageInfo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = info.firmwareVersion,
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

    private fun initBleDeviceStatus3(info: SystemRunStateInfo) {
        try {
            val groupList = mutableListOf<Any>()
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("供电信息"))

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "外部电压",
                value = info.externalVoltage.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                unit = "V"
            )

            val batteryCapacity =
                info.batteryVoltage.replace("%", "").toDoubleOrNull() ?: Double.MAX_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "内部电量",
                value = info.batteryVoltage.replace("%", "").ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                unit = "%",
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

    private fun initBleDeviceStatus4(info: DeviceStatusInfoTwo) {
        try {
            val groupList = mutableListOf<Any>()
            if (info.internalTempHumidityStatus != "2") {
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部温度",
                    value = info.internalTemperature,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部湿度",
                    value = info.internalHumidity,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 2,
                    unit = "%",
                    isBottomItem = info.externalTempHumidityStatus != "2"
                )
            }
            if (info.externalTempHumidityStatus != "2") {
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "外部温度",
                    value = info.externalTemperature,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "外部湿度",
                    value = info.externalHumidity,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 2,
                    unit = "%",
                    isBottomItem = true
                )
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