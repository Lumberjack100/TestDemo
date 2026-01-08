package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
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
 * @time: 2026/01/08
 * @desc: GT600 设备基本信息
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment
 * 2. 展示'设备信息'分组：SN、固件版本、硬件版本、累计运行时间
 * 3. 展示'存储信息'分组：可用空间、总空间。单位为 GB
 */
class GT600BaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    /**
     * 初始化状态信息 - 处理 GT600 设备状态数据
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 解析状态信息
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 1. 设备信息组
                groupList.add(DeviceStatusInfoGroupItem("设备信息"))

                // 设备SN
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )

                // 固件版本
                stateInfo.sw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "固件版本",
                        value = stateInfo.sw_version,
                    )
                }

                // 硬件版本
                stateInfo.attach_data?.forEach { info ->
                    when (info.key) {
                        "hw_version" -> {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "硬件版本",
                                    value = info.value as? String ?: AppContants.PLACE_HOLDER_VALUE,
                                    isBottomItem = true
                                )
                            )
                        }
                    }
                }


                // 2. 存储信息组
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("存储信息"))

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "可用空间",
                    value = AppContants.PLACE_HOLDER_VALUE,
                    unit = "GB"
                )

                // 总空间 (单位 GB)
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "总空间",
                    value = AppContants.PLACE_HOLDER_VALUE,
                    unit = "GB",
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }
}
