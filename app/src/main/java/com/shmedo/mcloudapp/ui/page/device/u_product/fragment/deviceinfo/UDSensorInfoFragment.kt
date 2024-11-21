package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
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

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=2")
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
                val externalVoltage = stateInfo.externalVoltage.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.externalVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == -1000.0) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池状态",
                    value = when (stateInfo.batteryStatus) {
                        "0" -> "放电中"
                        "1" -> "充电中"
                        "2" -> "已充满"
                        "-1" -> "异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.batteryStatus == "-1") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0,
                )

                val batteryVoltage = stateInfo.batteryVoltage.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电压",
                    value = if (batteryVoltage == 0.0) "0" else stateInfo.batteryVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                )

                val batteryCapacity = stateInfo.batteryCapacity.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电量",
                    value = stateInfo.batteryCapacity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "%",
                    textColorRes = if (batteryCapacity > 25 || batteryCapacity == -1000.0) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                )

                val batteryTemp = stateInfo.batteryTemp.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池温度",
                    value = stateInfo.batteryTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if (batteryTemp < 80) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "℃",
                )

                val batteryHealth = stateInfo.batteryHealth.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "最大容量",
                    value = stateInfo.batteryHealth.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if (batteryHealth > 70 || batteryHealth == -1000.0) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = stateInfo.internalTemp.toDoubleOrNull() ?: -1000.0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.internalTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if ((internalTemp > -20 && internalTemp < 70) || batteryHealth == -1000.0) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
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
                        "模块信息", bgColorRes = ColorUtils.getColor(R.color.main_bg_gray)
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "雷达模块",
                    value = when (stateInfo.ldStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.ldStatus == "-2" || stateInfo.ldStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "摄像头模块",
                    value = when (stateInfo.cameraStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.cameraStatus == "-2" || stateInfo.cameraStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "GNSS模块",
                    value = when (stateInfo.gnssStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.gnssStatus == "-2" || stateInfo.gnssStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "倾角模块",
                    value = when (stateInfo.accelerometerStatus) {
                        "-3" -> "模块异常"
                        "-2" -> "数据异常"
                        "0" -> "正常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.accelerometerStatus == "-2" || stateInfo.accelerometerStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "4G模块",
                    value = when (stateInfo._4gStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo._4gStatus == "-2" || stateInfo._4gStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙模块",
                    value = when (stateInfo.btStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.btStatus == "-2" || stateInfo.btStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台模块",
                    value = when (stateInfo.radioStatus) {
                        "0" -> "正常"
                        "-1" -> "未连接"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        "-4" -> "未开启"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.radioStatus == "-2" || stateInfo.radioStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0,
                    isBottomItem = true
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "存储模块",
                    value = when (stateInfo.flashStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.flashStatus == "-2" || stateInfo.flashStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0,
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "温湿度模块",
                    value = when (stateInfo.athStatus) {
                        "0" -> "正常"
                        "-2" -> "数据异常"
                        "-3" -> "模块异常"
                        else -> AppContants.PLACE_HOLDER_VALUE
                    },
                    textColorRes = if (stateInfo.athStatus == "-2" || stateInfo.athStatus == "-3") ColorUtils.getColor(
                        R.color.error_FF4400
                    ) else 0,
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