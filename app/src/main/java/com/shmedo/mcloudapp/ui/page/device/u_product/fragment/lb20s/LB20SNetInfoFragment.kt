package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 无线预警广播(LB20S)网络信息
 */
class LB20SNetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
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

                // 数据网络信息组
                groupList.add(DeviceStatusInfoGroupItem("数据网络"))
                stateInfo._4g_signal.notNullKey {
                    var temp = it
                    if (temp !in -110..-50) {
                        temp = 0
                    }
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "信号强度",
                            signalValue = temp,
                            textColorRes = if (temp == 0) ColorUtils.getColor(
                                R.color.error_FF4400
                            ) else 0
                        )
                    )
                }
                stateInfo.attach_data?.get("imei")?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "IMEI",
                            value = it,
                            isClipboard = true
                        )
                    )
                }
                stateInfo.attach_data?.get("iccid")?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "ICCID",
                            value = it,
                            isClipboard = true,
                            isBottomItem = true
                        )
                    )
                }

                // 数据链路信息组
                if (stateInfo.datacenterStatus != IOTConstants.NULL_KEY) {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                    
                    // 根据逗号分隔状态信息
                    val statusList = stateInfo.datacenterStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    statusList.forEachIndexed { index, status ->
                        val statusText = status.compareAndReturn(
                            "1",
                            "已连接",
                            status.compareAndReturn("2", "未连接", "未启用")
                        )

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "数据链路${index + 1}",
                            value = statusText,
                            textColorRes = if (status == "0" || statusText == "未连接") 0 else ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ),
                            isClickable = true,
                            isBottomItem = index == statusList.size - 1
                        )
                    }
                }

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


    override fun processItemClick(infoBasicItem: DeviceStatusInfoBasicItem) {
        if (infoBasicItem.name.startsWith("数据链路")) {
            val index = infoBasicItem.name.substringAfter("数据链路").toIntOrNull() ?: 0
            val item = DataCenterStatusItem(
                centerid = index,
                name = "数据链路$index",
                status = when {
                    infoBasicItem.value.contains("未启用") -> "0"
                    infoBasicItem.value.contains("已连接") -> "1"
                    infoBasicItem.value.contains("未连接") -> "2"
                    else -> "0"
                }
            )

            val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                item,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_dataCenterParamFragment,
                bundle
            )
        }
    }
}