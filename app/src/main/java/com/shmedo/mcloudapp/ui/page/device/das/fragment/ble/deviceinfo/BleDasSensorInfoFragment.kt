package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.shmedo.lib.cmd.base.iot_cmd.enums.SensorErrorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDRainStation
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoThree
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.cmd.base.md_cmd.model.das.InclinometerInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/6/11
 * 描述： TODO
 */
class BleDasSensorInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        binding.recyclerview.models = mutableListOf()

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
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_2 -> {//##042\r\n：查询设备状态2
                val result = mdParseManager.parse<DeviceStatusInfoTwo>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_2
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询开关量传感器信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        setDeviceStatusTwo(result.data)
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
                        setInclinometerInfo(result.data)
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
                        initExternalSensorData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun setDeviceStatusTwo(info: DeviceStatusInfoTwo) {
        try {
            val groupList = mutableListOf<Any>()
            groupList.add(DeviceStatusInfoGroupItem("开关量传感器"))

            when (MDRainStation.value(info.switchType)) {
                MDRainStation.CLOSE -> {//2：关闭
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "未接入",
                            textColorRes = ColorUtils.getColor(R.color.red_F13838)
                        )
                    )
                }

                MDRainStation.RAIN_OPEN -> {//1：雨量计
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "接入",
                            textColorRes = ColorUtils.getColor(R.color.green_00B26B)
                        )
                    )
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "雨量值(毫米)",
                            value = info.rainfallStatus
                        )
                    )
                }

                MDRainStation.ALARM_OPEN -> {//3：断线报警器
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "接入",
                            textColorRes = ColorUtils.getColor(R.color.green_00B26B)
                        )
                    )
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "断线报警器",
                            value = if (info.rainfallStatus == "1" || info.rainfallStatus == "1.0") "断线" else "未断线",
                            textColorRes = if (info.rainfallStatus == "1" || info.rainfallStatus == "1.0") ColorUtils.getColor(
                                R.color.red_F13838
                            ) else ColorUtils.getColor(
                                R.color.green_00B26B
                            )
                        )
                    )
                }
            }
            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 设置倾角计信息
     */
    private fun setInclinometerInfo(info: InclinometerInfo) {
        try {
            if (info.status == "2")
                return

            val groupList = mutableListOf<Any>()
            groupList.add(DeviceStatusInfoGroupItem("倾角计"))
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "状态",
                    value = if (info.status == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                        info.status
                    ),
                    textColorRes = if (info.status == "0") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else ColorUtils.getColor(
                        R.color.red_F13838
                    )
                )
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "X轴角度(°)",
                value = info.xAxis,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Y轴角度(°)",
                value = info.yAxis,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "Z轴角度(°)",
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
            )
            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initExternalSensorData(info: DeviceStatusInfoThree) {
        try {
            if (info.collectorAddress == "0" || info.sensorStatus.isNullOrEmpty()) {
                if (binding.recyclerview.models?.isEmpty() == true)
                    binding.refreshLayout.showEmpty()
                return
            }
            binding.refreshLayout.showContent()
            binding.recyclerview.mutable.add(DeviceStatusInfoGroupItem("扩展传感器"))
            for (i in 0 until info.sensorStatus.size) {
                val tempStr = info.sensorStatus[i]
                //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，2 不展示 ③：传感器数据
                val tempList = tempStr.split(":").dropLastWhile { it.isEmpty() }
                if (tempList.size < 3 || tempList[1] == "2") {
                    continue
                }
                binding.recyclerview.mutable.add(
                    DasSensorStatusInfo(
                        type = info.collectorModel,
                        addr = tempList[0].toIntOrNull() ?: 0,
                        errno = tempList[1].toIntOrNull() ?: 0,
                        valueList = tempList.subList(2, tempList.size)
                    )
                )
                binding.recyclerview.mutable.add(
                    GapItem(
                        height = ConvertUtils.dp2px(8f)
                    )
                )
            }
            if (binding.recyclerview.models?.isEmpty() == true)
                binding.refreshLayout.showEmpty()
            else
                binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = BleDasSensorInfoFragment()
    }
}