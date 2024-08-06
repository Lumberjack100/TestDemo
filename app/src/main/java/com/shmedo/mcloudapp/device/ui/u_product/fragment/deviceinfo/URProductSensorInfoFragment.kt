package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc:  雨量计传感器状态
 *
 */
class URProductSensorInfoFragment : BaseDeviceStatusInfoFragment() {

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

                groupList.add(DeviceStatusInfoGroupItem("雨量计"))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "雨量传感器状态",
                        value = "正常",
                        textColorRes =  ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "dayRain" -> {//24小时雨量值
                            if (info.value.isNotEmpty()) {
                                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                    groupList,
                                    name = "24小时雨量值",
                                    value = info.value,
                                    defaultValue = "0",
                                    digit = 1,
                                    unit = "mm",
                                )
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
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外部供电电压",
                    value = stateInfo.ext_power_volt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = URProductSensorInfoFragment()
    }
}