package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 无线预警广播(LB20S)状态信息
 */
class LB20SStatusInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content as String)
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

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = stateInfo.ext_power_volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.ext_power_volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
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

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}