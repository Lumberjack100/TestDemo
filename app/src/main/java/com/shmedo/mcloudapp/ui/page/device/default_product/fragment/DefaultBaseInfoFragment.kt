package com.shmedo.mcloudapp.ui.page.device.default_product.fragment

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
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 基本信息
 */
class DefaultBaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
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

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                stateInfo.sn.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "设备SN",
                        value = stateInfo.sn,
                    )
                }

                stateInfo.self_check.notNullKey {
                    val deviceAbnormalList =
                        if (stateInfo.self_check.isEmpty()) arrayListOf<String>() else DeviceStatusHelper.checkDeviceAbnormal(
                            stateInfo.self_check
                        )
                    val deviceStatus = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备状态",
                            value = deviceStatus,
                            textColorRes = when (deviceStatus) {
                                "正常" -> ColorUtils.getColor(R.color.online_colorPrimary)
                                "告警" -> ColorUtils.getColor(
                                    R.color.warn_FF9D00
                                )

                                else -> ColorUtils.getColor(R.color.error_FF4400)
                            }
                        )
                    )
                }
                stateInfo.hw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "硬件版本",
                        value = stateInfo.hw_version,
                    )
                }
                stateInfo.sw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "固件版本",
                        value = stateInfo.sw_version,
                    )
                }
                stateInfo.worktime.notNullKey {
                    stateInfo.worktime.toIntOrNull()?.let {
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "累计运行时间",
                                value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3),
                                isBottomItem = true
                            )
                        )
                    }
                }

                stateInfo.workMode.notNullKey {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "工作模式",
                        value = when (stateInfo.workMode) {
                            "1" -> "基站"
                            "2" -> "测站"
                            "3" -> "PPP-B2b"
                            "4" -> "CORS接入"
                            else -> AppContants.PLACE_HOLDER_VALUE
                        },
                        isBottomItem = true
                    )
                }

                stateInfo.emmc_storage.notNullKey {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("存储信息"))

                    val storages = it.replace("MB", "").split(",")
                    if (storages.size == 2) {
                        val free = DeviceStatusInfoProcessor.formatDoubleValue(
                            storages[0], "0", 1
                        )
                        val total = DeviceStatusInfoProcessor.formatDoubleValue(
                            storages[1], "0", 1
                        )
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "可用空间",
                            value = free,
                            unit = "MB"
                        )
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "总空间",
                            value = total,
                            unit = "MB",
                            isBottomItem = true
                        )
                    }
                }

                stateInfo.eMMCFree.notNullKey {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("存储信息"))

                    val storages = it.replace("MB", "")
                    val free = DeviceStatusInfoProcessor.formatDoubleValue(
                        storages, "0", 1
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "可用空间",
                        value = free,
                        unit = "MB",
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