package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ul

import android.os.Bundle
import android.util.Log
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/8/6
 * 描述： 北斗林木生长监测终端基本信息
 */
class ULBaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

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
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 设备信息组
                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "产品型号",
                    value = productType.productToken,
                )

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

                stateInfo.productTime.notNullKey {
                    stateInfo.productTime.toIntOrNull()?.let { workTimeSeconds ->
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "生产日期",
                                value = formatProductTime(stateInfo.productTime),
                                isBottomItem = true
                            )
                        )
                    }
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 将产品时间格式 (YYMMDD) 转换为可读的年月日格式
     * @param productTime 产品时间，格式为 YYMMDD，如 250801
     * @return 格式化后的日期字符串，如 "2025年 8月 1日"
     */
    private fun formatProductTime(productTime: String): String {
        return try {
            if (productTime.length == 6) {
                val year = "20${productTime.substring(0, 2)}"
                val month = productTime.substring(2, 4).toInt()
                val day = productTime.substring(4, 6).toInt()
                "${year}年 ${month}月 ${day}日"
            } else {
                productTime
            }
        } catch (e: Exception) {
            Timber.e(e, "格式化产品时间失败: $productTime")
            productTime
        }
    }
}