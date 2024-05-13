package com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo

import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.formatDoubleValue
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/5/12
 * @desc: 预警广播
 *
 */
class LB20SBaseInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content)
                }
                if (dataMap.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val stateInfo = dataMap["000_1"]
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()
                stateInfo.attach_data?.get("SN")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备SN", value = it))
                }
                stateInfo.attach_data?.get("imei")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMEI", value = it))
                }
                stateInfo.attach_data?.get("imsi")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMSI", value = it))
                }
                stateInfo.attach_data?.get("iccid")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备ICCID", value = it))
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
                if (stateInfo.ext_power_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.ext_power_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "外部电源电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
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
                stateInfo.attach_data?.get("volumelevel")?.let { volumeLevelStr ->
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "音量等级",
                            value = if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无"
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("太阳能控制器"))
                if (stateInfo.ext_power_volt != IOTConstants.NULL_KEY)
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "正常",
                            colorRes = ColorUtils.getColor(R.color.device_online_platform)
                        )
                    )
                if (stateInfo.solar_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.solar_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "太阳能板电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                if (stateInfo.battery_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.battery_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "蓄电池电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
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
        fun newInstance() = LB20SBaseInfoFragment()
    }
}