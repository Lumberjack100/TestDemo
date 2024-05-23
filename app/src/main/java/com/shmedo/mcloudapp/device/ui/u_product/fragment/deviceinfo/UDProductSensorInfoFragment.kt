package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
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

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 泥位计传感器状态
 *
 */
class UDProductSensorInfoFragment : BaseDeviceStatusInfoFragment() {
    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
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

                groupList.add(DeviceStatusInfoGroupItem("泥位计"))
                stateInfo.cam.notNullKey {
                    val camState = if (stateInfo.cam.uppercase().contains("OK")) "正常" else "异常"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "摄像头状态",
                            value = camState,
                            textColorRes = if (camState == "正常") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                stateInfo.ld.notNullKey {
                    val camState = if (stateInfo.ld.uppercase().contains("OK")) "正常" else "异常"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "雷达状态",
                            value = camState,
                            textColorRes = if (camState == "正常") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "安装高度",
                    value = stateInfo.height,
                    defaultValue = "0",
                    digit =3,
                    unit = "m",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "雷达测量值",
                    value = stateInfo.ldValue,
                    defaultValue = "0",
                    digit =3,
                    unit = "m",
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }


    companion object {
        fun newInstance() = UDProductSensorInfoFragment()
    }
}