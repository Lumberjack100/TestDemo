package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import com.blankj.utilcode.util.ColorUtils
import com.shmedo.mcloudapp.ext.formatDoubleValue
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
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
                if (stateInfo.cam != IOTConstants.NULL_KEY) {
                    val camState = if (stateInfo.cam.uppercase().contains("OK")) "正常" else "异常"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "摄像头状态",
                            value = camState,
                            colorRes = if (camState == "正常") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                if (stateInfo.ld != IOTConstants.NULL_KEY) {
                    val camState = if (stateInfo.ld.uppercase().contains("OK")) "正常" else "异常"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "雷达状态",
                            value = camState,
                            colorRes = if (camState == "正常") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                                R.color.device_offline_platform
                            )
                        )
                    )
                }
                decimalFormat.applyPattern("#.###")
                if (stateInfo.height != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.height.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "安装高度(米)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
                if (stateInfo.ldValue != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.ldValue.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "雷达测量值(米)",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    companion object {
        fun newInstance() = UDProductSensorInfoFragment()
    }
}