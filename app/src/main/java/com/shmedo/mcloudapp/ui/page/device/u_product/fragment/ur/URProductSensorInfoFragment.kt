package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ur

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc:  一体化雨量计传感器状态
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
                        textColorRes = ColorUtils.getColor(R.color.online_colorPrimary)
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
                            info.value.toIntOrNull()?.let {
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "连续运行时间",
                                        value = DeviceStatusInfoProcessor.millis2FitTimeSpan(
                                            it * 1000L,
                                            3
                                        )
                                    )
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
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = URProductSensorInfoFragment()
    }
}