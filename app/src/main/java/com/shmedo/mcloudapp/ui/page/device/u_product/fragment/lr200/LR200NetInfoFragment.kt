package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lr200

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
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
 * 描述： 一体式裂缝计网络信息
 */
class LR200NetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
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

                groupList.add(DeviceStatusInfoGroupItem("数据网络"))
                stateInfo._4g_signal.notNullKey {
                    var temp = it.toDoubleOrNull()?.toInt() ?: 0
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

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "IMEI",
                    value = stateInfo.iMEI,
                    isClipboard = true
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "ICCID",
                    value = stateInfo.iCCID,
                    isClipboard = true,
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                val status1 = stateInfo.dataCenter1.compareAndReturn(
                    "0", "未启用", stateInfo.dataCenter1.compareAndReturn("1", "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路1",
                    value = status1,
                    textColorRes = if (stateInfo.dataCenter1 == "0" || status1 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ),
                    isClickable = true,
                )

                val status2 = stateInfo.dataCenter2.compareAndReturn(
                    "0", "未启用", stateInfo.dataCenter2.compareAndReturn("1", "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路2",
                    value = status2,
                    textColorRes = if (stateInfo.dataCenter2 == "0" || status2 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ),
                    isClickable = true,
                )

                val status3 = stateInfo.dataCenter3.compareAndReturn(
                    "0", "未启用", stateInfo.dataCenter3.compareAndReturn("1", "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路3",
                    value = status3,
                    textColorRes = if (stateInfo.dataCenter3 == "0" || status3 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ),
                    isClickable = true,
                )

                val status4 = stateInfo.dataCenter4.compareAndReturn(
                    "0", "未启用", stateInfo.dataCenter4.compareAndReturn("1", "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路4",
                    value = status4,
                    textColorRes = if (stateInfo.dataCenter4 == "0" || status4 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ),
                    isClickable = true,
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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