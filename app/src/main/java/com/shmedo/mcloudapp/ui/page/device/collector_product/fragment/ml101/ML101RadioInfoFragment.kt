package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import android.os.Bundle
import android.util.Log
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50RadioParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 电台信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 展示电台信息分组：
 *    - 功能开关（开/关）
 *    - 收发频点（470-508MHz）
 *    - 发射功率（0-20）
 *    - 空中速率（2.4kbps/19.2kbps/76.8kbps）
 *    - 本机地址（1-65535）
 *    - 目标地址（0-65535）
 *
 * 数据来源：
 * - M50_MD_RADIO_PARAM (md_cfgradioparam) - 查询电台参数
 *
 * 指令格式：
 * 发送：$cmd=md_cfgradioparam&method=0
 * 应答：$cmd=md_cfgradioparam&method=0&sw=1&mode=3&baud=6&freq_group=0&airbaud=2&...
 *
 * 参考实现：
 * - MS101SatInfoFragment.kt - 页面结构和基类使用
 * - M50RadioSettingFragment.kt - 频率和速率转换逻辑
 */
class ML101RadioInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 常量定义 ====================

    /**
     * 频率选项映射
     * freq_group 取值范围 0-19，对应频率 470-508MHz（间隔 2MHz）
     */
    private val frequencyLabels = (0..19).map { index ->
        "${470.41 + index * 2}MHz"
    }

    /**
     * 空中速率选项映射
     * airbaud 取值：1 -> 2.4kbps, 2 -> 19.2kbps, 3 -> 76.8kbps
     */
    private val airRateLabels = mapOf(
        "1" to "2.4kbps",
        "2" to "19.2kbps",
        "3" to "76.8kbps"
    )

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "电台信息"
    }

    /**
     * 查询电台参数信息
     *
     * 发送指令：M50_MD_RADIO_PARAM (md_cfgradioparam&method=0)
     * method=0 表示查询当前参数
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 构建查询电台参数指令
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.M50_MD_RADIO_PARAM,
                "method=0"
            )
        )

        // 发送指令序列
        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    // ==================== 指令响应处理 ====================

    /**
     * 处理指令响应
     *
     * 根据指令类型分发到对应的处理方法：
     * - M50_MD_RADIO_PARAM：调用 initStatusInfo() 初始化电台信息
     *
     * @param cmdStr 指令响应字符串
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M50_MD_RADIO_PARAM -> {
                // 解析电台参数
                val result = iotParseManager.parse<M50RadioParam>(
                    cmdStr,
                    IOTCommandType.M50_MD_RADIO_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询电台参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    // ==================== UI 初始化方法 ====================

    /**
     * 初始化电台信息
     *
     * 该方法负责构建页面 UI，包括以下字段：
     * 1. 功能开关：开/关
     * 2. 收发频点：470-508MHz
     * 3. 发射功率：0-20
     * 4. 空中速率：2.4kbps/19.2kbps/76.8kbps
     * 5. 本机地址：1-65535
     * 6. 目标地址：0-65535
     *
     * @param content M50RadioParam 电台参数信息对象
     */
    override fun <T> initStatusInfo(content: T) {
        try {
            val radioParam = content as M50RadioParam

            // 显示内容区域
            binding.refreshLayout.showContent()

            // 构建 UI 列表
            val groupList = mutableListOf<Any>()

            // 添加电台信息分组标题
            groupList.add(DeviceStatusInfoGroupItem("电台信息"))

            // ==================== 功能开关 ====================
            // sw: 0 -> 关, 1 -> 开
            val switchText = if (radioParam.sw == "1") "开" else "关"
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "功能开关",
                value = switchText
            )

            // ==================== 收发频点 ====================
            // freq_group: 0-19 对应 470-508MHz（间隔 2MHz）
            val frequencyText = radioParam.freq_group.toIntOrNull()?.let { index ->
                frequencyLabels.getOrNull(index)
            } ?: AppContants.PLACE_HOLDER_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "收发频点",
                value = frequencyText
            )

            // ==================== 发射功率 ====================
            // txpower: 0-20，直接显示原值
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "发射功率",
                value = radioParam.txpower.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // ==================== 空中速率 ====================
            // airbaud: 1 -> 2.4kbps, 2 -> 19.2kbps, 3 -> 76.8kbps
            val airRateText = airRateLabels[radioParam.airbaud] ?: AppContants.PLACE_HOLDER_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "空中速率",
                value = airRateText
            )

            // ==================== 本机地址 ====================
            // local_addr: 1-65535
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "本机地址",
                value = radioParam.local_addr.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // ==================== 目标地址 ====================
            // target_addr: 0-65535（0 表示广播模式）
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "目标地址",
                value = radioParam.target_addr.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                isBottomItem = true // 最后一项
            )

            // 更新 RecyclerView 数据
            binding.recyclerview.models = groupList

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
            binding.refreshLayout.showError()
        }
    }
}
