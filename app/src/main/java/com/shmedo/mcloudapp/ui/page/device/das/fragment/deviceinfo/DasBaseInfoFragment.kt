package com.shmedo.mcloudapp.ui.page.device.das.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasSolarStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasTemperatureAndHumidityStatusinfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/6/11
 * 描述： TODO
 */
class DasBaseInfoFragment : BaseDeviceStatusInfoFragment() {
    override fun queryStatusInfo() {
        binding.recyclerview.models = mutableListOf()
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SOLAR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (!isResumed) {
            return
        }
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
                        initBaseInfo(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_SOLAR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SOLAR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询太阳能控制器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initSolarStatus(result.data)
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
                        initTemperatureAndHumidityStatus(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBaseInfo(baseInfo: DasBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = baseInfo.sn,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备ICCID",
                value = baseInfo.iccid,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备IMEI",
                value = baseInfo.imei,
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
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备位置",
                value = baseInfo.local,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                groupList,
                name = "内部电量",
                value = baseInfo.involt.replace("%", ""),
                defaultValue = "0",
                thresHold = 10.0,
                digit = 2,
                unit = "%",
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                groupList,
                name = "外部供电电压",
                value = baseInfo.outvolt,
                defaultValue = "0",
                thresHold = 5.0,
                digit = 2,
                unit = "V",
            )
            baseInfo.csq.notNullKey {
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

    /**
     * 太阳能控制器
     */
    private fun initSolarStatus(content: String) {
        launchWithViewLifecycle {
            try {
                if (content.isEmpty() || content == "{}") {
                    return@launchWithViewLifecycle
                }
                val info = MoshiUtil.fromJson<DasSolarStatusInfo>(content)
                    ?: return@launchWithViewLifecycle

                if (info.solar.errno.isEmpty()) {
                    return@launchWithViewLifecycle
                }

                val groupList = mutableListOf<Any>()
                groupList.add(DeviceStatusInfoGroupItem("太阳能控制器"))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "状态",
                        value = if (info.solar.errno == "1") "正常" else "异常",
                        textColorRes = if (info.solar.errno == "1") ColorUtils.getColor(
                            R.color.device_online_platform
                        ) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "太阳能板电压",
                    value = info.solar.solarvolt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "蓄电池电压",
                    value = info.solar.batvolt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "太阳能功率",
                    value = info.solar.solarpwr,
                    unit = "W",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "负载功率",
                    value = info.solar.loadpwr,
                    unit = "W",
                )
                binding.recyclerview.mutable.addAll(groupList)
                binding.recyclerview.bindingAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 温湿度状态
     */
    private fun initTemperatureAndHumidityStatus(content: String) {
        launchWithViewLifecycle {
            try {
                if (content.isEmpty() || content == "{}") {
                    return@launchWithViewLifecycle
                }
                val info = MoshiUtil.fromJson<DasTemperatureAndHumidityStatusinfo>(content)
                    ?: return@launchWithViewLifecycle

                val groupList = mutableListOf<Any>()
                if (info.inth.errno.isNotEmpty()) {
                    groupList.add(DeviceStatusInfoGroupItem("机箱内部温湿度"))
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = if (info.inth.errno == "1") "正常" else "异常",
                            textColorRes = if (info.inth.errno == "1") ColorUtils.getColor(
                                R.color.device_online_platform
                            ) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "温度",
                        value = info.inth.temp,
                        defaultValue = "0",
                        digit = 2,
                        unit = "℃",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "湿度",
                        value = info.inth.humi,
                        defaultValue = "0",
                        digit = 2,
                        unit = "%",
                    )
                }
                if (info.outth.errno.isNotEmpty()) {
                    groupList.add(DeviceStatusInfoGroupItem("机箱外部温湿度"))
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = if (info.outth.errno == "1") "正常" else "异常",
                            textColorRes = if (info.outth.errno == "1") ColorUtils.getColor(
                                R.color.device_online_platform
                            ) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "温度",
                        value = info.outth.temp,
                        defaultValue = "0",
                        digit = 2,
                        unit = "℃",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                        groupList,
                        name = "湿度",
                        value = info.outth.humi,
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
    }

    companion object {
        fun newInstance() = DasBaseInfoFragment()
    }
}