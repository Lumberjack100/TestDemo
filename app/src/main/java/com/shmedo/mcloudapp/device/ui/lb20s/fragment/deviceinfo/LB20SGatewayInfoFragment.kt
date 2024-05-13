package com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo

import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LB20SGatewayInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "devicetype=1")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                }
                if (commonCurrentStateInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val stateInfo = commonCurrentStateInfoList[0]
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                if (stateInfo.sn != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备SN",
                            value = stateInfo.sn
                        )
                    )
                }
                if (stateInfo.productDate != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "生产日期",
                            value = stateInfo.productDate
                        )
                    )
                }
                if (stateInfo.rttVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "系统版本",
                            value = stateInfo.rttVersion
                        )
                    )
                }
                if (stateInfo.rttVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "系统版本",
                            value = stateInfo.rttVersion
                        )
                    )
                }
                if (stateInfo.hardwareVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "硬件版本",
                            value = stateInfo.hardwareVersion
                        )
                    )
                }
                if (stateInfo.firmwareVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "固件版本",
                            value = stateInfo.firmwareVersion
                        )
                    )
                }
                if (stateInfo.lora != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "LoRa状态",
                            value = if (stateInfo.lora.uppercase().contains("OK")) "OK" else "FAIL",
                            colorRes = if (stateInfo.lora.uppercase()
                                    .contains("OK")
                            ) ColorUtils.getColor(R.color.text_color_3AD094) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                if (stateInfo.loraVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "LoRa版本",
                            value = stateInfo.loraVersion
                        )
                    )
                }
                if (stateInfo.bt != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "蓝牙状态",
                            value = if (stateInfo.bt.uppercase().contains("OK")) "OK" else "FAIL",
                            colorRes = if (stateInfo.bt.uppercase()
                                    .contains("OK")
                            ) ColorUtils.getColor(R.color.text_color_3AD094) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                if (stateInfo.btVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "蓝牙版本",
                            value = stateInfo.btVersion
                        )
                    )
                }
                if (stateInfo.radio != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电台状态",
                            value = if (stateInfo.radio.uppercase()
                                    .contains("OK")
                            ) "OK" else "FAIL",
                            colorRes = if (stateInfo.radio.uppercase()
                                    .contains("OK")
                            ) ColorUtils.getColor(R.color.text_color_3AD094) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                if (stateInfo.radioVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电台版本",
                            value = stateInfo.radioVersion
                        )
                    )
                }
                if (stateInfo.radioEUI != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电台EUI",
                            value = stateInfo.radioEUI
                        )
                    )
                }
                if (stateInfo.flash != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "flash状态",
                            value = if (stateInfo.flash.uppercase()
                                    .contains("OK")
                            ) "OK" else "FAIL",
                            colorRes = if (stateInfo.flash.uppercase()
                                    .contains("OK")
                            ) ColorUtils.getColor(R.color.text_color_3AD094) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                if (stateInfo.location != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备位置",
                            value = stateInfo.location
                        )
                    )
                }
                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    companion object {
        fun newInstance() = LB20SGatewayInfoFragment()
    }
}