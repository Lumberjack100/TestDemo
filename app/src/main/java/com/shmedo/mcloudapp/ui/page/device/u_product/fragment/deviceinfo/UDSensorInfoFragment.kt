package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
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
class UDSensorInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("状态信息")
    }

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
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = stateInfo.externalVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电压",
                    value = stateInfo.batteryStatus.compareAndReturn(
                        "0",
                        stateInfo.batteryVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        "电池异常"
                    ),
                    textColorRes = if (stateInfo.batteryStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电量",
                    value = stateInfo.batteryStatus.compareAndReturn(
                        "0",
                        stateInfo.batteryCapacity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        "电池异常"
                    ),
                    textColorRes = if (stateInfo.batteryStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池温度",
                    value = stateInfo.batteryStatus.compareAndReturn(
                        "0",
                        stateInfo.batteryTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        "电池异常"
                    ),
                    textColorRes = if (stateInfo.batteryStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "充放状态",
                    value = stateInfo.batteryStatus.compareAndReturn(
                        "0",
                        if (stateInfo.batteryChargeStatus == "1") "充电中" else "放电中",
                        "电池异常"
                    ),
                    textColorRes = if (stateInfo.batteryStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池健康度",
                    value = stateInfo.batteryStatus.compareAndReturn(
                        "0",
                        stateInfo.batteryHealth.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        "电池异常"
                    ),
                    textColorRes = if (stateInfo.batteryStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.internalTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.internalHumidity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "%",
                )
                groupList.add(
                    DeviceStatusInfoGroupItem(
                        "传感信息",
                        bgColorRes = ColorUtils.getColor(R.color.main_bg_gray)
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "雷达状态",
                    value = when (stateInfo.ldStatus) {
                        "-3" -> "模块异常"
                        "-2" -> "数据异常"
                        "0" -> "正常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.ldStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "海拔高度",
                    value = stateInfo.altitude,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 3,
                    unit = "m",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "摄像头状态",
                    value = when (stateInfo.cameraStatus) {
                        "-3" -> "异常"
                        "0" -> "正常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.cameraStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
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
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.accelerometerStatus == "0") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}