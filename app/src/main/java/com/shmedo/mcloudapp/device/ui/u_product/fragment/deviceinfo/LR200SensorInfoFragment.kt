package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.formatDoubleValue
import timber.log.Timber

class LR200SensorInfoFragment : BaseDeviceStatusInfoFragment() {

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
                groupList.add(DeviceStatusInfoGroupItem("倾角计"))
                if (stateInfo.x_Angle != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.x_Angle.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "X轴角度(°)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                if (stateInfo.y_Angle != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.y_Angle.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "Y轴角度(°)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                if (stateInfo.z_Angle != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.z_Angle.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "Z轴角度(°)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("裂缝计"))
                if (stateInfo.lF_initial != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.lF_initial.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "初始测量值(mm)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                if (stateInfo.lF_current != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.lF_current.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "实时测量值(mm)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                if (stateInfo.lF_Cumulative != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.lF_Cumulative.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "累计变化量(mm)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
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
        fun newInstance() = LR200SensorInfoFragment()
    }
}