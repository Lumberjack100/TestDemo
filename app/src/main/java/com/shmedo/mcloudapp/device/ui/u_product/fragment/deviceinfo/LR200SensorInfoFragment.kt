package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
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
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "X轴角度",
                    value = stateInfo.x_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "Y轴角度",
                    value = stateInfo.y_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "Z轴角度",
                    value = stateInfo.z_Angle,
                    defaultValue = "0",
                    digit = 2,
                    unit = "°",
                )
                stateInfo.lF_initial.notNullKey {
                    groupList.add(DeviceStatusInfoGroupItem("裂缝计"))
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "初始测量值",
                    value = stateInfo.lF_initial,
                    defaultValue = "0",
                    digit = 2,
                    unit = "mm",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "实时测量值",
                    value = stateInfo.lF_current,
                    defaultValue = "0",
                    digit = 2,
                    unit = "mm",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "累计变化量",
                    value = stateInfo.lF_Cumulative,
                    defaultValue = "0",
                    digit = 2,
                    unit = "mm",
                )

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