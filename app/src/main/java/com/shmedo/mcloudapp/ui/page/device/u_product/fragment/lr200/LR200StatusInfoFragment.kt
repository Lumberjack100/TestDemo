package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lr200

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 一体式裂缝计状态信息
 */
class LR200StatusInfoFragment : BaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("电池信息"))
                val batteryVoltage =
                    stateInfo.battery_volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电压",
                    value = if (batteryVoltage == 0.0) "0" else stateInfo.battery_volt.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                )

                val batteryCapacity =
                    stateInfo.volt_percent.replace("%", "").toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电量",
                    value = stateInfo.volt_percent.replace("%", "").ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "%",
                    textColorRes = if (batteryCapacity > 25) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = stateInfo.temp.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.temp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if ((internalTemp > -20 && internalTemp < 70) || internalTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "℃",
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.humidity.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))
                stateInfo.self_check.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "LORA模块",
                        value = if (it.uppercase().contains("LORA:1")) "有" else "无",
                        textColorRes = if (it.uppercase()
                                .contains("LORA:1")
                        ) ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                            R.color.error_FF4400
                        ),
                        isBottomItem = true
                    )
                }

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}