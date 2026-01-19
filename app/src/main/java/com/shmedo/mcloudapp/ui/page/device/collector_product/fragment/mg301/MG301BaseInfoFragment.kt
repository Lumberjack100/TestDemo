package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mg301

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.mg301.MG301DeviceStatusInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/16
 * 描述：MG301 多模融合通讯数据网关设备基本信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 展示 '设备信息' 分组：设备型号、设备SN、固件版本、硬件版本、生产日期
 * 3. 展示 '存储信息' 分组：可用空间、总空间（单位 MB）
 *
 * 数据来源：
 * - 使用 MD_GET_DEVICE_STATUS (md_getdevicesta) 指令查询设备状态
 * - JSON 响应数据结构参考 MG301DeviceStatusInfo 类
 *
 * 参考实现：
 * - M20SBaseInfoFragment.kt
 * - GT600BaseInfoFragment.kt
 */
class MG301BaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "基本信息"
    }

    // ==================== 数据处理 ====================

    /**
     * 初始化状态信息 - 处理 MG301 设备状态数据
     *
     * 该方法在基类接收到指令响应后被调用，负责：
     * 1. 解析 JSON 数据为 MG301DeviceStatusInfo 对象
     * 2. 构建设备信息和存储信息分组列表项
     * 3. 更新 RecyclerView 显示
     *
     * @param content 设备状态信息的 JSON 字符串（由 DeviceCurrentStateParser2 解析 state 字段获取）
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据，避免阻塞主线程
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<MG301DeviceStatusInfo>(content as String)
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

                // 添加各信息分组
                addDeviceInfoGroup(groupList, statusInfo)
                addStorageInfoGroup(groupList, statusInfo)

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
     * 包含字段：
     * - 设备型号：从 productType 获取
     * - 设备SN：从 statusInfo.sn 获取
     * - 固件版本：从 statusInfo.firmwareVersion 获取
     * - 硬件版本：从 statusInfo.hardwareVersion 获取
     * - 生产日期：从 statusInfo.productDate 获取并格式化
     *
     * @param groupList 列表项容器
     * @param statusInfo MG301 设备状态信息对象
     */
    private fun addDeviceInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: MG301DeviceStatusInfo
    ) {
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))

        // 设备型号 - 从页面参数中获取设备产品类型
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "设备型号",
            value = productType.productToken
        )

        // 设备SN
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "设备SN",
            value = deviceInfo.deviceToken
        )

        // 固件版本
        statusInfo.firmwareVersion.notNullKey {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = statusInfo.firmwareVersion
            )
        }

        // 硬件版本
        statusInfo.hardwareVersion.notNullKey {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "硬件版本",
                value = statusInfo.hardwareVersion
            )
        }

        // 生产日期 - 需要格式化（20260106 -> 2026-01-06）
        val formattedDate = statusInfo.getFormattedProductDate()
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "生产日期",
            value = formattedDate,
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 添加存储信息分组
     *
     * 包含字段：
     * - 可用空间：从 statusInfo.emmcFree 获取，单位 MB
     * - 总空间：从 statusInfo.emmcStorage 获取，单位 MB
     *
     * 注意：MG301 的存储信息单位为 MB（与 M20S 一致）
     *
     * @param groupList 列表项容器
     * @param statusInfo MG301 设备状态信息对象
     */
    private fun addStorageInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: MG301DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("存储信息"))

        // 可用空间 - 内部存储剩余容量
        val freeSpace = statusInfo.getFormattedEmmcFree()
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "可用空间",
            value = freeSpace.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "MB"
        )

        // 总空间 - 内部存储总容量
        val totalSpace = statusInfo.getFormattedEmmcStorage()
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "总空间",
            value = totalSpace.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "MB",
            isBottomItem = true // 该分组的最后一项
        )
    }
}
