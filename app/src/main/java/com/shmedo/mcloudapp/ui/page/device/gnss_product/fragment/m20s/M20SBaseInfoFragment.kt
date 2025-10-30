package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/12/19
 * @desc: 普适型 GNSS 接收机(M20S)基本信息
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyle2Fragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的M20S特定业务逻辑不变
 * 5. 支持4G和蓝牙两种通讯方式
 */
class M20SBaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    /**
     * 初始化状态信息 - 处理M20S设备状态数据
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 设备信息组
                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )

                // 硬件版本
                stateInfo.hw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "硬件版本",
                        value = stateInfo.hw_version,
                    )
                }

                // 固件版本
                stateInfo.sw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "固件版本",
                        value = stateInfo.sw_version,
                    )
                }

                // 累计运行时间
                stateInfo.worktime.notNullKey {
                    stateInfo.worktime.toIntOrNull()?.let { workTimeSeconds ->
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "累计运行时间",
                                value = DeviceStatusInfoProcessor.millis2FitTimeSpan(workTimeSeconds * 1000L, 3),
                                isBottomItem = true
                            )
                        )
                    }
                }

                // 存储信息组（方式一：emmc_storage格式）
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

                // 存储信息组（方式二：eMMCFree格式）
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
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }
} 