package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.ext.formatDoubleValue
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import timber.log.Timber

class LR200BaseInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo =
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                if (stateInfo.sN != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备SN",
                            value = stateInfo.sN
                        )
                    )
                }
                if (stateInfo.iMEI != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备IMEI",
                            value = stateInfo.iMEI
                        )
                    )
                }
                if (stateInfo.iMSI != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备IMSI",
                            value = stateInfo.iMSI
                        )
                    )
                }
                if (stateInfo.cCID != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备ICCID",
                            value = stateInfo.cCID
                        )
                    )
                }
                if (stateInfo.hw_version != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "硬件版本",
                            value = stateInfo.hw_version
                        )
                    )
                }
                if (stateInfo.sw_version != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "固件版本",
                            value = stateInfo.sw_version
                        )
                    )
                }
                if (stateInfo.location != IOTConstants.NULL_KEY)
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备位置",
                            value = stateInfo.location
                        )
                    )

                if (stateInfo.lF_range != IOTConstants.NULL_KEY)
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "测量范围",
                            value = stateInfo.lF_range + "mm"
                        )
                    )
                if (stateInfo.volt_percent != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.volt_percent.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电池电量",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "%",
                            colorRes = if (tempValue <= 10) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                if (stateInfo.temp != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.temp.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "内部温度",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "℃"
                        )
                    )
                }
                if (stateInfo.humidity != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.humidity.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "内部湿度",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "%"
                        )
                    )
                }
                if (stateInfo.temp_out != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.temp_out.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "外部温度",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "℃"
                        )
                    )
                }
                if (stateInfo.humidity_out != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.humidity_out.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "外部湿度",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "%"
                        )
                    )
                }
                groupList.add(
                    DeviceStatusInfoSignalItem(
                        name = "4G信号强度",
                        signalValue = stateInfo._4g_signal.let {
                            if (it <= 0)
                                it
                            else
                                it * 2 - 113
                        })
                )
                if (stateInfo.self_check != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "LoRa功能",
                            value = if (stateInfo.self_check.uppercase()
                                    .contains("LORA:1")
                            ) "有" else "无",
                            colorRes = if (stateInfo.self_check.uppercase()
                                    .contains("LORA:1")
                            ) ColorUtils.getColor(R.color.text_color_3AD094) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
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
        fun newInstance() = LR200BaseInfoFragment()
    }
}