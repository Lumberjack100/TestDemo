package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.MS101SatParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：MS101 卫通信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 展示卫通信息分组：
 *    - 接入状态（已接入/未接入）
 *    - 序列号
 *    - 信号质量（差/一般/较好/好/无）
 *    - 环境噪声信号强度 (dBm)
 *    - 待发数据数量
 *
 * 数据来源：
 * - MD_CFG_SAT_PARAM (md_cfgsatparam) - 查询卫通参数
 *
 * 指令格式：
 * 发送：$cmd=md_cfgsatparam&method=0
 * 应答：$cmd=md_cfgsatparam&method=0&status=1&sn=0001836&cesq=-10&csq=-66&waitnum=240&...
 *
 * 参考实现：
 * - MG301NetInfoFragment.kt - 卫通信息分组展示
 * - ML101BaseInfoFragment.kt - 页面结构和基类使用
 */
class MS101SatInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "卫通信息"
    }

    /**
     * 查询卫通参数信息
     *
     * 发送指令：MD_CFG_SAT_PARAM (md_cfgsatparam&method=0)
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询卫通参数（method=0 表示查询）
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_CFG_SAT_PARAM,
                "method=0"
            )
        )

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
     * - MD_CFG_SAT_PARAM：调用 initStatusInfo() 初始化卫通信息
     *
     * @param cmdStr 指令响应字符串
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_CFG_SAT_PARAM -> {
                // 解析卫通参数
                val result = iotParseManager.parse<MS101SatParamInfo>(
                    cmdStr,
                    IOTCommandType.MD_CFG_SAT_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询卫通参数出错: ${result.message}"
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
     * 初始化卫通信息
     *
     * 该方法负责构建页面 UI，包括以下字段：
     * 1. 接入状态：已接入（绿色）/未接入
     * 2. 序列号：设备序列号
     * 3. 信号质量：差/一般/较好/好/无
     * 4. 环境噪声信号强度 (dBm)
     * 5. 待发数据数量
     *
     * @param content MS101SatParamInfo 卫通参数信息对象
     */
    override fun <T> initStatusInfo(content: T) {
        try {
            val satParamInfo = content as MS101SatParamInfo

            // 显示内容区域
            binding.refreshLayout.showContent()

            // 构建 UI 列表
            val groupList = mutableListOf<Any>()

            // 添加卫通信息分组
            groupList.add(DeviceStatusInfoGroupItem("卫通信息"))

            // ==================== 接入状态 ====================
            // 已接入显示绿色，未接入显示默认颜色
            val statusText = if (satParamInfo.isConnected()) "已接入" else "未接入"
            val statusColor = if (satParamInfo.isConnected()) {
                ColorUtils.getColor(R.color.online_colorPrimary)
            } else {
                0
            }
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "接入状态",
                value = statusText,
                textColorRes = statusColor
            )

            // ==================== 序列号 ====================
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "序列号",
                value = satParamInfo.sn.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // ==================== 信号质量 ====================
            // 根据 cesq 值判断信号质量等级：差/一般/较好/好/无
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "信号质量",
                value = satParamInfo.getSignalQualityLevel()
            )

            // ==================== 环境噪声信号强度 ====================
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "环境噪声信号强度(dBm)",
                value = satParamInfo.csq.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // ==================== 待发数据数量 ====================
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "待发数据数量",
                value = if (satParamInfo.waitnum >= 0) {
                    satParamInfo.waitnum.toString()
                } else {
                    AppContants.PLACE_HOLDER_VALUE
                },
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
