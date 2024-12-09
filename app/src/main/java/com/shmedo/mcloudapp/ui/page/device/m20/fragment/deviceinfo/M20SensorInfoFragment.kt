package com.shmedo.mcloudapp.ui.page.device.m20.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.extensions.notNullKey
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
                    val camState = if (it.uppercase().contains("MEMS:0")) "异常" else "正常"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "倾角MEMS状态",
                            value = camState,
                            textColorRes = if (camState == "正常") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                                R.color.error_FF4400
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
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = M20SensorInfoFragment()
    }
}