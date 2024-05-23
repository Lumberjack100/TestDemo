package com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class M20SensorInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("倾角计"))
                stateInfo.self_check.notNullKey {
                    val camState = if (it.uppercase().contains("MEMS:1")) "正常" else "异常"
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
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "X轴角度值",
                    value = stateInfo.x_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "Y轴角度值",
                    value = stateInfo.y_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "Z轴角度值",
                    value = stateInfo.z_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = M20SensorInfoFragment()
    }
}