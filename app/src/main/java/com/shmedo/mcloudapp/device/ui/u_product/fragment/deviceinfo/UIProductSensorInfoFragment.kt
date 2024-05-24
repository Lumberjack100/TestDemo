package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.GapItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 倾斜仪传感器状态
 *
 */
class UIProductSensorInfoFragment : BaseDeviceStatusInfoFragment() {
    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val uRSensorInfoList = stateInfo.attach_data
                if (uRSensorInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("倾角计"))
                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "memsstatus" -> {
                            val camState = if (info.value.contains("1")) "正常" else "异常"
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "倾角MEMS状态",
                                    value = camState,
                                    textColorRes = if (camState == "正常") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                                        R.color.device_offline_platform
                                    )
                                )
                            )
                        }

                        "initAngle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val initAngle = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (initAngle.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴初始角度值",
                                        value = initAngle[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (initAngle.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴初始角度值",
                                        value = initAngle[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (initAngle.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴初始角度值",
                                        value = initAngle[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "angle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val angle = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (angle.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴当前角度值",
                                        value = angle[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (angle.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴当前角度值",
                                        value = angle[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (angle.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴当前角度值",
                                        value = angle[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "acc" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val acc = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (acc.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴加速度值",
                                        value = acc[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                if (acc.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴加速度值",
                                        value = acc[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                if (acc.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴加速度值",
                                        value = acc[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "worktime" -> {
                            if (info.value.isNotEmpty()) {
                                val day = info.value.toInt() / (24 * 60 * 60)
                                val hour = (info.value.toInt() % (24 * 60 * 60)) / (60 * 60)
                                val minute = (info.value.toInt() % (60 * 60)) / 60
                                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                                    groupList,
                                    name = "连续运行时间",
                                    value = "${day}天${hour}时${minute}分",
                                )
                            }
                        }
                    }
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = UIProductSensorInfoFragment()
    }
}