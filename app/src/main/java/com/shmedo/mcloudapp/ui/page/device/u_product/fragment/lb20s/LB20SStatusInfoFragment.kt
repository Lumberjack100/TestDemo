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
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 无线预警广播(LB20S)状态信息
 */
class LB20SStatusInfoFragment : BaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val cmdContent = content as String
                
                // 兼容处理老固件和新固件的返回数据
                val stateInfo = withContext(Dispatchers.IO) {
                    parseStateInfo(cmdContent)
                }
                
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 供电信息组
                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = stateInfo.ext_power_volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.ext_power_volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    isBottomItem = true
                )

                // 环境信息组
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

    /**
     * 解析状态信息，兼容老固件和新固件格式
     * 老固件格式：{"000_1": { LB20SCurrentStateInfo数据 }}
     * 新固件格式：直接是 LB20SCurrentStateInfo 数据
     */
    private suspend fun parseStateInfo(jsonContent: String): LB20SCurrentStateInfo? {
        return try {
            // 首先尝试解析老固件格式
            val dataMap = MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(jsonContent)
            if (!dataMap.isNullOrEmpty() && dataMap.containsKey("000_1")) {
                Timber.d("解析为老固件格式数据")
                dataMap["000_1"]
            } else {
                // 如果老固件格式解析失败或没有期望的key，尝试新固件格式
                Timber.d("尝试解析为新固件格式数据")
                MoshiUtil.fromJson<LB20SCurrentStateInfo>(jsonContent)
            }
        } catch (e: Exception) {
            // 如果老固件格式解析失败，尝试新固件格式
            try {
                Timber.d("老固件格式解析失败，尝试解析为新固件格式数据")
                MoshiUtil.fromJson<LB20SCurrentStateInfo>(jsonContent)
            } catch (e2: Exception) {
                Timber.e(e2, "无法解析设备状态信息")
                null
            }
        }
    }

}