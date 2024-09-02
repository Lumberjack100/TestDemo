package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.UDCommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 一体化雷达泥位计电池、传感器等信息
 *
 */
class UDSensorInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "value=2")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCommonCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外部电压",
                    value = stateInfo.externalVoltage,
                    defaultValue = "--",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "电池电压",
                    value = stateInfo.batteryVoltage,
                    defaultValue = "--",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电量",
                    value = stateInfo.batteryCapacity,
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "温度",
                    value = stateInfo.batteryTemp,
                    defaultValue = "--",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "充放状态",
                    value = if (stateInfo.batteryStatus == "1") "充电中" else "放电中",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池健康",
                    value = stateInfo.batteryHealth,
                    unit = "%",
                )
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.internalTemp,
                    defaultValue = "--",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.internalTemp,
                    defaultValue = "--",
                    digit = 2,
                    unit = "%",
                )
                groupList.add(DeviceStatusInfoGroupItem("传感信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "雷达状态",
                    value = when (stateInfo.ldStatus) {
                        "-3" -> "模块异常"
                        "-2" -> "数据异常"
                        "0" -> "正常"
                        else -> "--"
                    },
                    textColorRes = if (stateInfo.ldStatus == "0") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                        R.color.red_F13838
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "海拔高度",
                    value = stateInfo.altitude,
                    defaultValue = "--",
                    digit = 3,
                    unit = "m",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "摄像头状态",
                    value = when (stateInfo.cameraStatus) {
                        "-3" -> "异常"
                        "0" -> "正常"
                        else -> "--"
                    },
                    textColorRes = if (stateInfo.cameraStatus == "0") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                        R.color.red_F13838
                    )
                )
                if (stateInfo.pixx != IOTConstants.NULL_KEY
                    && stateInfo.pixy != IOTConstants.NULL_KEY
                ) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "图片分辨率",
                        value = "${stateInfo.pixx}x${stateInfo.pixy}",
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "加速度计",
                    value = when (stateInfo.accelerometerStatus) {
                        "-3" -> "模块异常"
                        "-2" -> "数据异常"
                        "0" -> "正常"
                        else -> "--"
                    },
                    textColorRes = if (stateInfo.accelerometerStatus == "0") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                        R.color.red_F13838
                    )
                )

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = UDSensorInfoFragment()
    }
}