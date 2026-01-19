package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mg301

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mg301.MG301SatModInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/19
 * 描述：MG301 多模融合通讯数据网关网络信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 展示 '4G 网络' 分组：信号强度、IMEI、ICCID
 * 3. 展示 '数据链路' 分组：数据链路 1-4 的连接状态（未启用/已连接/未连接）
 * 4. 展示 '卫通信息' 分组：接入状态、序列号、卫星信号质量、环境噪声信号强度、待发数据数量
 * 5. 展示 '北斗短报文' 分组：暂无对应指令，字段使用占位符
 *
 * 数据来源：
 * - QUERY_DEVICE_STATUS (getstatus) - 获取 4G 网络和数据链路状态
 * - MD_GET_SATMOD (md_getsatmod) - 获取卫通模块状态
 *
 * 参考实现：
 * - MR702NetInfoFragment.kt - 数据链路状态展示和点击跳转
 * - DasNetInfoFragment.kt - 多指令查询处理
 * - MG301BaseInfoFragment.kt - 页面结构和基类使用
 */
class MG301NetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "网络信息"
    }

    /**
     * 查询设备状态信息
     *
     * 发送指令序列：
     * 1. QUERY_DEVICE_STATUS - 查询 4G 网络和数据链路状态
     * 2. MD_GET_SATMOD - 查询卫通模块状态
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询设备状态（4G 网络和数据链路）
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS))

        // 查询卫通模块状态
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.MD_GET_SATMOD))

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
     * 根据不同的指令类型分发到对应的处理方法：
     * - QUERY_DEVICE_STATUS：调用 initStatusInfo() 初始化 4G 网络和数据链路
     * - MD_GET_SATMOD：调用 initSatModInfo() 初始化卫通信息
     *
     * @param cmdStr 指令响应字符串
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                // 解析设备状态（4G 网络和数据链路）
                val result = iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_SATMOD -> {
                // 解析卫通模块状态
                val result = iotParseManager.parse<MG301SatModInfo>(cmdStr, IOTCommandType.MD_GET_SATMOD)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询卫通模块状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initSatModInfo(result.data)
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
     * 初始化设备状态信息 - 处理 QUERY_DEVICE_STATUS 响应
     *
     * 该方法负责构建以下UI分组：
     * 1. 4G 网络分组：信号强度、IMEI、ICCID
     * 2. 数据链路分组：数据链路 1-4 的连接状态
     * 3. 北斗短报文分组：暂无对应指令，使用占位符
     *
     * @param content 设备状态信息的 JSON 字符串
     */
    override fun <T> initStatusInfo(content: T) {
        try {
            // 提取 state 字段
            val stateStr = content as String
            if (stateStr.isNullOrEmpty()) {
                binding.refreshLayout.showError()
                return
            }

            // 显示内容区域
            binding.refreshLayout.showContent()

            // 构建 UI 列表
            val groupList = mutableListOf<Any>()

            // 添加各信息分组
            add4GNetworkGroup(groupList, stateStr)
            addDataLinkGroup(groupList, stateStr)

            // 更新 RecyclerView 数据
            binding.recyclerview.models = groupList

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
            binding.refreshLayout.showError()
        }
    }

    /**
     * 初始化卫通模块信息 - 处理 MD_GET_SATMOD 响应
     *
     * 该方法负责构建卫通信息分组，包括：
     * - 接入状态
     * - 序列号
     * - 卫星信号质量
     * - 环境噪声信号强度
     * - 待发数据数量
     *
     * @param satModInfo 卫通模块状态信息
     */
    private fun initSatModInfo(satModInfo: MG301SatModInfo) {
        try {
            val groupList = mutableListOf<Any>()

            // 添加分组间隔
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            // 添加分组标题
            groupList.add(DeviceStatusInfoGroupItem("卫通信息"))

            // 接入状态
            val statusText = if (satModInfo.isConnected()) "已接入" else "未接入"
            val statusColor = if (satModInfo.isConnected()) {
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

            // 序列号
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "序列号",
                value = satModInfo.sn.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // 卫星信号质量
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "信号质量",
                value = satModInfo.getSignalQualityLevel()
            )

            // 环境噪声信号强度
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "环境噪声信号强度(dBm)",
                value = satModInfo.csq.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
            )

            // 待发数据数量
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "待发数据数量",
                value = if (satModInfo.waitnum >= 0) satModInfo.waitnum.toString() else AppContants.PLACE_HOLDER_VALUE,
                isBottomItem = true
            )

            addBeidouGroup(groupList)


            // 追加到 RecyclerView
            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    // ==================== 分组构建方法 ====================

    /**
     * 添加 4G 网络分组
     *
     * 包含字段：
     * - 信号强度：从 state 中的 4g_signal 字段获取
     * - IMEI：从 attach_data 中的 imei 获取
     * - ICCID：从 attach_data 中的 iccid 获取
     *
     * @param groupList 列表项容器
     * @param stateStr state 字段的 JSON 字符串
     */
    private fun add4GNetworkGroup(groupList: MutableList<Any>, stateStr: String) {
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("4G网络"))

        // 解析 4G 信号强度
        val signal4gPattern = "\"4g_signal\":(-?[0-9]+)".toRegex()
        val signal4g = signal4gPattern.find(stateStr)?.groupValues?.get(1) ?: "0"
        var signal4gValue = signal4g.toIntOrNull() ?: 0
        if (signal4gValue !in -110..-50) {
            signal4gValue = 0
        }
        groupList.add(
            DeviceStatusInfoSignalItem(
                name = "信号强度",
                signalValue = signal4gValue,
                textColorRes = if (signal4gValue == 0) ColorUtils.getColor(
                    R.color.error_FF4400
                ) else 0
            )
        )

        // 解析 attach_data 获取 IMEI 和 ICCID
        var imei = AppContants.PLACE_HOLDER_VALUE
        var iccid = AppContants.PLACE_HOLDER_VALUE

        try {
            // 提取 attach_data 数组
            val attachDataPattern = "\"attach_data\":\\[(.*?)]".toRegex(RegexOption.DOT_MATCHES_ALL)
            val attachDataMatch = attachDataPattern.find(stateStr)
            if (attachDataMatch != null) {
                val attachDataStr = attachDataMatch.groupValues[1]

                // 提取 IMEI
                val imeiPattern = "\\{\"key\":\"imei\",\"value\":\"([^\"]+)\"\\}".toRegex()
                imei = imeiPattern.find(attachDataStr)?.groupValues?.get(1) ?: AppContants.PLACE_HOLDER_VALUE

                // 提取 ICCID
                val iccidPattern = "\\{\"key\":\"iccid\",\"value\":\"([^\"]+)\"\\}".toRegex()
                iccid = iccidPattern.find(attachDataStr)?.groupValues?.get(1) ?: AppContants.PLACE_HOLDER_VALUE
            }
        } catch (e: Exception) {
            Timber.e(e, "解析 attach_data 失败")
        }

        // IMEI
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "IMEI",
            value = imei,
            isClipboard = true
        )

        // ICCID
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "ICCID",
            value = iccid,
            isClipboard = true,
            isBottomItem = true
        )
    }

    /**
     * 添加数据链路分组
     *
     * 包含字段：
     * - 数据链路 1：从 attach_data 中的 Socket1 获取
     * - 数据链路 2：从 attach_data 中的 Socket2 获取
     * - 数据链路 3：从 attach_data 中的 Socket3 获取
     * - 数据链路 4：从 attach_data 中的 Socket4 获取
     *
     * 状态值说明：
     * - 0：未启用
     * - 1：已连接
     * - 2：未连接
     *
     * @param groupList 列表项容器
     * @param stateStr state 字段的 JSON 字符串
     */
    private fun addDataLinkGroup(groupList: MutableList<Any>, stateStr: String) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("数据链路"))

        // 解析 attach_data 获取 Socket 状态
        val socketStates = mutableMapOf<Int, String>()

        try {
            // 提取 attach_data 数组
            val attachDataPattern = "\"attach_data\":\\[(.*?)]".toRegex(RegexOption.DOT_MATCHES_ALL)
            val attachDataMatch = attachDataPattern.find(stateStr)
            if (attachDataMatch != null) {
                val attachDataStr = attachDataMatch.groupValues[1]

                // 提取 Socket1-4 状态
                for (i in 1..4) {
                    val socketPattern = "\\{\"key\":\"Socket$i\",\"value\":\"([0-2])\"\\}".toRegex()
                    val socketMatch = socketPattern.find(attachDataStr)
                    if (socketMatch != null) {
                        socketStates[i] = socketMatch.groupValues[1]
                    } else {
                        socketStates[i] = "0" // 默认未启用
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "解析 Socket 状态失败")
        }

        // 添加数据链路 1-4 状态
        for (i in 1..4) {
            val socketStatus = socketStates[i] ?: "0"
            val statusText = socketStatus.compareAndReturn(
                "0",
                "未启用",
                socketStatus.compareAndReturn("1", "已连接", "未连接")
            )
            val statusColor = if (socketStatus == "0" || statusText == "未连接") {
                0
            } else {
                ColorUtils.getColor(R.color.online_colorPrimary)
            }

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路$i",
                value = statusText,
                textColorRes = statusColor,
                isClickable = true,
                isBottomItem = (i == 4) // 最后一项
            )
        }
    }

    /**
     * 添加北斗短报文分组
     *
     * 注意：暂时没有相应的指令获取北斗短报文数据，所有字段使用占位符
     *
     * 包含字段：
     * - 数传终端
     * - 信号强度
     * - 信号值(dB)
     * - 北斗卡号
     *
     * @param groupList 列表项容器
     */
    private fun addBeidouGroup(groupList: MutableList<Any>) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("北斗短报文"))

        // 数传终端 - 使用占位符
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "数传终端",
            value = AppContants.PLACE_HOLDER_VALUE
        )

        // 信号强度 - 使用占位符
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "信号强度",
            value = AppContants.PLACE_HOLDER_VALUE
        )

        // 信号值(dB) - 使用占位符
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "信号值(dB)",
            value = AppContants.PLACE_HOLDER_VALUE
        )

        // 北斗卡号 - 使用占位符
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "北斗卡号",
            value = AppContants.PLACE_HOLDER_VALUE,
            isBottomItem = true
        )
    }

    // ==================== 交互处理 ====================

    /**
     * 处理列表项点击事件
     *
     * 当点击数据链路项时，跳转到数据中心参数配置页面
     *
     * @param infoBasicItem 被点击的列表项
     */
    override fun processItemClick(infoBasicItem: DeviceStatusInfoBasicItem) {
        if (infoBasicItem.name.startsWith("数据链路")) {
            // 提取数据链路编号
            val index = infoBasicItem.name.substringAfter("数据链路").toIntOrNull() ?: 0

            // 构建数据中心状态项
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

            // 跳转到数据中心参数配置页面
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
