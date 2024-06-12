package com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoOne
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.device.base.md_cmd.model.das.SystemRunStateInfo
import com.shmedo.lib.device.base.md_cmd.model.das.VersionMessageInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasBaseInfoFragment : BaseDeviceStatusInfoFragment() {
    /**
     * 获取设备的基本信息
     */
    override fun queryStatusInfo() {
        binding.recyclerview.models = mutableListOf()
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
        command =
            MDCommandUtil.getCommand(MDCommandType.SYSTEM_RUN_STATE)
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
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_2)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (!isResumed) {
            return
        }
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
                        initDeviceStatusOne(result.data)
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
                        initVersionInfo(result.data)
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
                        initOperatorInformation(result.data)
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
                        val errMsg = "查询太阳能、机箱温湿度状态出错"
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

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDeviceStatusOne(info: DeviceStatusInfoOne) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = info.snNumber,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备ICCID",
                value = info.simNumber,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备IMEI",
                value = info.imeiNumber,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备启动代码",
                value = "${MDCommandUtil.formatStringTwo(info.startCodeOne)}${
                    MDCommandUtil.formatStringTwo(info.startCodeTwo)
                }",
            )
            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initVersionInfo(info: VersionMessageInfo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = info.firmwareVersion,
            )
            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initOperatorInformation(info: SystemRunStateInfo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                groupList,
                name = "内部电量",
                value = getDeviceInternalBattery(
                    info.batteryVoltage.toDoubleOrNull() ?: 0.0
                ).replace("%", ""),
                defaultValue = "0",
                thresHold = 10.0,
                digit = 2,
                unit = "%",
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                groupList,
                name = "外部供电电压",
                value = info.externalVoltage,
                defaultValue = "0",
                thresHold = 5.0,
                digit = 2,
                unit = "V",
            )
            info.gprsSignal.notNullKey {
                val temp = it.toIntOrNull() ?: 0
                groupList.add(
                    DeviceStatusInfoSignalItem(
                        name = "4G信号强度",
                        signalValue = if (temp <= 0)
                            temp
                        else
                            temp * 2 - 113
                    )
                )
            }

            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun getDeviceInternalBattery(internalBattery: Double): String {
        return if (internalBattery <= 6) {
            "1%"
        } else {
            val result = ((internalBattery - 6) / (8.2 - 6) * 100).toInt()
            if (result > 100) {
                "100%"
            } else {
                "$result%"
            }
        }
    }

    private fun setDeviceStatusTwo(info: DeviceStatusInfoTwo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备位置",
                value = "${info.longitude},${info.latitude}",
            )

            if (info.solarControllerStatus != "2") {
                groupList.add(DeviceStatusInfoGroupItem("太阳能控制器"))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "状态",
                        value = if (info.solarControllerStatus != "1") "正常" else "异常",
                        textColorRes = if (info.solarControllerStatus != "1") ColorUtils.getColor(
                            R.color.device_online_platform
                        ) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "太阳能板电压",
                    value = info.solarPanelVoltage,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "蓄电池电压",
                    value = info.batteryVoltage,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "太阳能功率",
                    value = info.dailyPowerGeneration,
                    unit = "W",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "负载功率",
                    value = info.dailyPowerConsumption,
                    unit = "W",
                )
            }
            if (info.internalTempHumidityStatus != "2") {
                groupList.add(DeviceStatusInfoGroupItem("机箱内部温湿度"))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "状态",
                        value = if (info.internalTempHumidityStatus != "1") "正常" else "异常",
                        textColorRes = if (info.internalTempHumidityStatus != "1") ColorUtils.getColor(
                            R.color.device_online_platform
                        ) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "温度",
                    value = info.internalTemperature,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "湿度",
                    value = info.internalHumidity,
                    defaultValue = "0",
                    digit = 2,
                    unit = "%",
                )
            }
            if (info.externalTempHumidityStatus != "2") {
                groupList.add(DeviceStatusInfoGroupItem("机箱外部温湿度"))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "状态",
                        value = if (info.externalTempHumidityStatus != "1") "正常" else "异常",
                        textColorRes = if (info.externalTempHumidityStatus != "1") ColorUtils.getColor(
                            R.color.device_online_platform
                        ) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "温度",
                    value = info.externalTemperature,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "湿度",
                    value = info.externalHumidity,
                    defaultValue = "0",
                    digit = 2,
                    unit = "%",
                )
            }

            binding.recyclerview.mutable.addAll(groupList)
            binding.recyclerview.bindingAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = BleDasBaseInfoFragment()
    }
}