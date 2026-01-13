package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GT600DeviceStatusInfo
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
 * @author：gonghe
 * @time: 2026/01/08
 * @desc: GT600 设备基本信息
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment
 * 2. 展示'设备信息'分组：设备型号、设备SN、固件版本、硬件版本
 * 3. 展示'存储信息'分组：可用空间、总空间，单位为 MB
 *
 * 数据来源：
 * - 使用 getstatus 指令查询设备状态
 * - JSON 响应数据结构参考 GT600DeviceStatusInfo 类注释
 *
 * 参考实现：
 * - GT600StatusInfoFragment.kt
 */
class GT600BaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    // ==================== 数据处理 ====================

    /**
     * 初始化状态信息 - 处理 GT600 设备状态数据
     *
     * 该方法在基类接收到指令响应后被调用，负责：
     * 1. 解析 JSON 数据为 GT600DeviceStatusInfo 对象
     * 2. 构建各信息分组的列表项
     * 3. 更新 RecyclerView 显示
     *
     * @param content 设备状态信息的 JSON 字符串
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据，避免阻塞主线程
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<GT600DeviceStatusInfo>(content as String)
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
     * - 设备SN：从 deviceInfo 获取
     * - 固件版本：从 sw_version 获取（通过 attach_data）
     * - 硬件版本：从 attach_data.hw_version 获取
     */
    private fun addDeviceInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))

        // 设备型号
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
        statusInfo.sw_version.notNullKey {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = statusInfo.sw_version,
            )
        }

        // 硬件版本
        // 数据来源：attach_data.hw_version
        val hwVersion = statusInfo.getAttachValue("hw_version")
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "硬件版本",
            value = hwVersion.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 添加存储信息分组
     *
     * 包含字段：
     * - 可用空间：从 storageCardRawData 解析，单位 MB
     * - 总空间：从 storageCardRawData 解析，单位 MB
     *
     * 数据来源：
     * - attach_data.storage: 存储卡数据，格式为 "可用空间,总空间" (单位 MB)
     *
     * 数据处理逻辑：
     * 1. 检查存储卡状态，如果包含 "FAIL" 则所有空间显示为占位符
     * 2. 解析 storageCardRawData 字符串，按逗号分割获取可用空间和总空间
     * 3. 验证数据格式，确保有两个数值
     * 4. 空值或异常数据使用占位符显示
     */
    private fun addStorageInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("存储信息"))

        // 解析存储空间数据
        val (freeSpace, totalSpace) = parseStorageData(statusInfo)

        // 可用空间
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "可用空间",
            value = freeSpace.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "MB"
        )

        // 总空间
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "总空间",
            value = totalSpace.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "MB",
            isBottomItem = true // 该分组的最后一项
        )
    }

    // ==================== 数据解析方法 ====================

    /**
     * 解析存储空间数据
     *
     * 从设备状态信息中提取可用空间和总空间值
     *
     * @param statusInfo 设备状态信息对象
     * @return Pair<String, String> 依次为可用空间和总空间值 (单位: MB)
     *
     * 解析规则：
     * 1. 如果存储卡状态包含 "FAIL"，返回空字符串
     * 2. 解析 storageCardRawData 字符串 (格式: "可用MB,总MB")
     * 3. 验证分割后的数组长度为 2
     * 4. 将 MB 值转换为 MB (除以 1024)，保留 2 位小数
     * 5. 任何异常情况返回空字符串，由调用方使用占位符显示
     */
    private fun parseStorageData(statusInfo: GT600DeviceStatusInfo): Pair<String, String> {
        // 检查存储卡状态
        if (statusInfo.storageCardStatus.contains("FAIL", ignoreCase = true)) {
            return Pair("", "")
        }

        // 获取原始存储数据并清理 MB 后缀
        val rawData = statusInfo.storageCardRawData
            .replace("MB", "", ignoreCase = true)
            .trim()

        // 解析原始存储数据
        val storageValues = rawData.split(",")

        // 验证数据格式：必须包含两个数值（可用空间和总空间）
        return if (storageValues.size == 2) {
            val freeSpaceMB = storageValues[0].trim().toDoubleOrNull()
            val totalSpaceMB = storageValues[1].trim().toDoubleOrNull()

            if (freeSpaceMB != null && totalSpaceMB != null) {
//                val freeSpaceGB = String.format("%.2f", freeSpaceMB / 1024)
//                val totalSpaceGB = String.format("%.2f", totalSpaceMB / 1024)
                Pair(freeSpaceMB.toString(), totalSpaceMB.toString())
            } else {
                // 数值解析失败，返回空值
                Pair("", "")
            }
        } else {
            // 数据格式异常，返回空值
            Pair("", "")
        }
    }
}
