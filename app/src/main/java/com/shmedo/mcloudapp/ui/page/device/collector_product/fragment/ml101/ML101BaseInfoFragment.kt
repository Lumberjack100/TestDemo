package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import android.os.Bundle
import android.util.Log
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.ML101DeviceStatusInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：ML101/MS101 一体化通信终端基本信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 展示 '设备信息' 分组：设备型号、设备SN、固件版本、硬件版本、生产日期
 *
 * 数据来源：
 * - 使用 QUERY_DEVICE_STATUS (getstatus) 指令查询设备状态
 * - JSON 响应数据结构参考 ML101DeviceStatusInfo 类
 *
 * 产品说明：
 * - ML101 (ProductType.C_L_1): 一体化 LoRa 通信终端
 * - MS101 (ProductType.C_S_2): 一体化卫星通信终端
 * - 两款产品共用此页面，基本信息字段完全一致
 *
 * 参考实现：
 * - MG301BaseInfoFragment.kt
 * - GT600BaseInfoFragment.kt
 */
class ML101BaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "基本信息"
    }

    // ==================== 数据处理 ====================

    /**
     * 初始化状态信息 - 处理 ML101/MS101 设备状态数据
     *
     * 该方法在基类接收到指令响应后被调用，负责：
     * 1. 解析 JSON 数据为 ML101DeviceStatusInfo 对象
     * 2. 构建设备信息分组的列表项
     * 3. 更新 RecyclerView 显示
     *
     * @param content 设备状态信息的 JSON 字符串（由 DeviceCurrentStateParser 解析 state 字段获取）
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据，避免阻塞主线程
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<ML101DeviceStatusInfo>(content as String)
                }

                // 数据校验：确保状态信息不为空
                if (statusInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }

                // 显示内容区域
                binding.refreshLayout.showContent()

                // 构建 UI 列表
                val groupList = mutableListOf<Any>()

                // 添加设备信息分组
                addDeviceInfoGroup(groupList, statusInfo)

                // 更新 RecyclerView 数据
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
                binding.refreshLayout.showError()
            }
        }
    }

    // ==================== 分组构建方法 ====================

    /**
     * 添加设备信息分组
     *
     * 根据原型图展示以下字段：
     * - 设备型号：从 productType 获取（使用页面传入的产品类型，确保显示正确的型号）
     * - 设备SN：从 deviceInfo.deviceToken 获取（使用页面传入的设备信息，确保一致性）
     * - 固件版本：从 statusInfo.sw_version 获取
     * - 硬件版本：从 statusInfo.attach_data.hw_version 获取
     * - 生产日期：从 statusInfo.product_time 获取并格式化
     *
     * @param groupList 列表项容器
     * @param statusInfo ML101/MS101 设备状态信息对象
     */
    private fun addDeviceInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: ML101DeviceStatusInfo
    ) {
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))

        // 设备型号 - 从页面参数中获取设备产品类型，确保显示正确的型号（ML101 或 MS101）
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "设备型号",
            value = productType.productToken
        )

        // 设备SN - 从页面参数中获取设备标识，确保与设备列表显示一致
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "设备SN",
            value = deviceInfo.deviceToken
        )

        // 固件版本 - 从设备响应数据中获取
        statusInfo.sw_version.notNullKey {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = statusInfo.sw_version
            )
        }

        // 硬件版本 - 从 attach_data.hw_version 获取
        val hwVersion = statusInfo.hardwareVersion
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "硬件版本",
            value = hwVersion
        )

        // 生产日期 - 需要格式化（20260120 -> 2026-01-20）
        val formattedDate = statusInfo.getFormattedProductTime()
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "生产日期",
            value = formattedDate,
            isBottomItem = true // 该分组的最后一项
        )
    }
}
