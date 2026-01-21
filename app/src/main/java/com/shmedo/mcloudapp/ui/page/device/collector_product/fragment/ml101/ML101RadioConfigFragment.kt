package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m.M50RadioParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50RadioParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentMl101RadioConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ML101RadioConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：ML101 电台配置页面
 *
 * 功能说明：
 * - 配置 LoRa 电台参数（工作模式、收发频点、发射功率、空中速率、地址等）
 * - 支持中心节点和终端节点两种工作模式
 *
 * 核心业务逻辑：
 * - 中心节点模式：目标地址自动设为 0（广播模式），禁用输入
 * - 终端节点模式：目标地址可手动输入（点对点通信）
 *
 * 使用指令：IOTCommandType.M50_MD_RADIO_PARAM (md_cfgradioparam)
 * - 获取参数：method=0
 * - 设置参数：method=1
 *
 * 复用实体类：M50RadioParam（解析响应）、M50RadioParamEntity（构建请求）
 */
class ML101RadioConfigFragment : OptimizedBaseIOTDeviceFragment() {

    // ==================== 成员变量 ====================

    /** DataBinding 绑定对象 */
    private lateinit var binding: FragmentMl101RadioConfigBinding

    /** 工具栏 ViewModel */
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    /** 页面状态 ViewModel */
    private val mStates: ML101RadioConfigViewModel by viewModels()

    /** IOT 指令解析管理器 */
    private val iotParseManager: IOTParserManager by inject()

    // ==================== 选项配置 ====================

    /**
     * 通用选项数据类
     * @param code 选项代码值（发送给设备的值）
     * @param label 选项显示文本（界面显示的值）
     */
    private data class Option(val code: String, val label: String)

    /**
     * 工作模式选项
     * - 中心节点 (mode=0)：广播模式，所有同频设备均可收到
     * - 终端节点 (mode=1)：点对点模式，仅目标地址设备可收到
     *
     * 注意：根据需求文档，mode 实际取值为 0-3（TypeA-TypeD），
     * 这里简化为 0（中心节点）和 3（终端节点，使用 TypeD 模式）
     */
    private val workModeOptions = listOf(
        Option("0", "中心节点"),
        Option("3", "终端节点")
    )

    /**
     * 收发频点选项
     * 频率范围：470MHz ~ 508MHz，步进 2MHz，共 20 个信道
     * freq_group 取值：0-19 对应 470.41-508.41MHz
     */
    private val frequencyOptions = (0..19).map { index ->
        val freq = 470 + index * 2
        Option(index.toString(), "${freq}MHz")
    }

    /**
     * 发射功率选项
     * 功率范围：0 ~ 20，默认 20
     */
    private val txPowerOptions = (0..20).map { power ->
        Option(power.toString(), power.toString())
    }

    /**
     * 空中速率选项
     * - 1: 2.4kbps
     * - 2: 19.2kbps（默认）
     * - 3: 76.8kbps
     */
    private val airRateOptions = listOf(
        Option("1", "2.4Kbps"),
        Option("2", "19.2Kbps"),
        Option("3", "76.8Kbps")
    )

    // ==================== 生命周期方法 ====================

    /**
     * 配置 DataBinding
     * 绑定布局、ViewModel 和点击事件代理
     */
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ml101_radio_config,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    /**
     * 初始化视图
     * 设置工具栏标题、返回按钮事件、下拉刷新
     */
    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMl101RadioConfigBinding
        // 设置页面标题
        binding.llToolbar.toolbar.title = "电台配置"
        // 设置返回按钮事件
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        // 注册系统返回键事件
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        // 初始化下拉刷新
        initRefresh()
    }

    /**
     * 初始化下拉刷新配置
     * 禁用上拉加载更多，设置下拉刷新回调
     */
    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    /**
     * 初始化数据
     * 设置默认参数值并保存初始状态
     */
    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态，用于后续修改检测
        mStates.saveInitialState()
    }

    /**
     * 重置为默认参数
     * 根据需求文档设置各参数的默认值
     */
    private fun resetDefaultParams() {
        // 默认工作模式：终端节点
        mStates.updateWorkMode(workModeOptions[1].label)
        // 默认收发频点：472MHz（freq_group=1）
        mStates.frequency.set(frequencyOptions[1].label)
        // 默认发射功率：20
        mStates.txPower.set(txPowerOptions.last().label)
        // 默认空中速率：19.2kbps（airbaud=2）
        mStates.airRate.set(airRateOptions[1].label)
        // 默认本机地址：2
        mStates.localAddress.set("2")
        // 默认目标地址：1（终端节点模式下可编辑）
        mStates.targetAddress.set("1")
    }

    // ==================== 点击事件处理 ====================

    /**
     * 点击事件代理内部类
     * 处理所有用户交互事件
     */
    inner class ClickProxy : BaseClickProxy() {

        /**
         * 工作模式选择点击事件
         * 弹出底部选择器，切换中心节点/终端节点模式
         */
        fun onWorkModeChooseClick() {
            showBottomListPopup(
                options = workModeOptions,
                currentValue = mStates.workMode.get()
            ) { selectedLabel ->
                // 使用 ViewModel 的方法更新工作模式
                // 该方法会自动处理目标地址的启用/禁用逻辑
                mStates.updateWorkMode(selectedLabel)
            }
        }

        /**
         * 收发频点选择点击事件
         * 弹出底部选择器，选择 470-508MHz 范围内的频点
         */
        fun onFrequencyChooseClick() {
            showBottomListPopup(
                options = frequencyOptions,
                currentValue = mStates.frequency.get()
            ) { selectedLabel ->
                mStates.frequency.set(selectedLabel)
            }
        }

        /**
         * 发射功率选择点击事件
         * 弹出底部选择器，选择 0-20 范围内的功率值
         */
        fun onTxPowerChooseClick() {
            showBottomListPopup(
                options = txPowerOptions,
                currentValue = mStates.txPower.get()
            ) { selectedLabel ->
                mStates.txPower.set(selectedLabel)
            }
        }

        /**
         * 空中速率选择点击事件
         * 弹出底部选择器，选择空中传输速率
         */
        fun onAirRateChooseClick() {
            showBottomListPopup(
                options = airRateOptions,
                currentValue = mStates.airRate.get()
            ) { selectedLabel ->
                mStates.airRate.set(selectedLabel)
            }
        }

        /**
         * 恢复默认配置点击事件
         * 将所有参数重置为默认值
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        /**
         * 保存按钮点击事件
         * 校验输入参数后发送配置指令
         */
        override fun onSubmitButtonClick() {
            // 隐藏软键盘
            KeyboardUtils.hideSoftInput(binding.root)
            // 检查设备连接状态
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            // 校验输入参数
            if (!validateInput()) {
                return
            }
            // 发送配置指令
            saveRadioParam()
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 显示底部列表选择弹窗
     * @param options 选项列表
     * @param currentValue 当前选中值
     * @param onSelected 选择回调
     */
    private fun showBottomListPopup(
        options: List<Option>,
        currentValue: String,
        onSelected: (String) -> Unit
    ) {
        val labels = options.map { it.label }.toTypedArray()
        val selectedIndex = options.indexOfFirst { it.label == currentValue }
            .takeIf { it >= 0 } ?: 0

        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .asBottomList(
                "",
                labels,
                null,
                selectedIndex,
                { _, text -> onSelected(text) },
                0,
                R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    /**
     * 校验输入参数合法性
     * @return true: 校验通过，false: 校验失败
     */
    private fun validateInput(): Boolean {
        val local = mStates.localAddress.get()
        val target = mStates.targetAddress.get()

        // 校验本机地址
        if (local.isBlank()) {
            showMessageDialog("请输入本机地址")
            return false
        }
        if (!local.all { it.isDigit() }) {
            showMessageDialog("本机地址仅支持数字")
            return false
        }
        val localValue = local.toIntOrNull() ?: run {
            showMessageDialog("本机地址格式不正确")
            return false
        }
        if (localValue !in 1..65535) {
            showMessageDialog("本机地址取值范围为 1-65535")
            return false
        }

        // 校验目标地址（仅终端节点模式需要校验，中心节点自动为 0）
        if (mStates.isTargetAddressEnabled.value == true) {
            if (target.isBlank()) {
                showMessageDialog("请输入目标地址")
                return false
            }
            if (!target.all { it.isDigit() }) {
                showMessageDialog("目标地址仅支持数字")
                return false
            }
            val targetValue = target.toIntOrNull() ?: run {
                showMessageDialog("目标地址格式不正确")
                return false
            }
            if (targetValue !in 0..65535) {
                showMessageDialog("目标地址取值范围为 0-65535")
                return false
            }
            // 校验本机地址与目标地址不能相同
            if (localValue == targetValue) {
                showMessageDialog("本机地址与目标地址不能相同")
                return false
            }
        }

        return true
    }

    // ==================== 通信方法 ====================

    /**
     * 保存电台配置参数
     * 构建设置指令并发送到设备
     */
    private fun saveRadioParam() {
        // 获取当前选项的代码值（用于发送指令）
        val freqCode = frequencyOptions.firstOrNull { it.label == mStates.frequency.get() }?.code
            ?: frequencyOptions[1].code
        val txPowerCode = txPowerOptions.firstOrNull { it.label == mStates.txPower.get() }?.code
            ?: txPowerOptions.last().code
        val airRateCode = airRateOptions.firstOrNull { it.label == mStates.airRate.get() }?.code
            ?: airRateOptions[1].code

        // 构建电台参数配置实体
        // 注意：中心节点模式下，target_addr 已被自动设为 "0"
        val entity = M50RadioParamEntity(
            method = "1",  // 1: 设置参数
            sw = "1",  // 工作开关始终开启
            freq_group = freqCode,
            airbaud = airRateCode,
            txpower = txPowerCode,
            local_addr = mStates.localAddress.get(),
            target_addr = mStates.targetAddress.get()
        )

        // 发送指令
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.M50_MD_RADIO_PARAM,
            entity.toCommandString()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 延迟加载数据
     * 页面可见时自动触发下拉刷新获取当前配置
     */
    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询当前电台配置
     * 发送获取参数指令（method=0）
     */
    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.M50_MD_RADIO_PARAM,
            "method=0"
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false,  // 使用刷新动画而不是加载弹窗
                errorConfig = ErrorConfig.dialogConfig()  // 查询失败显示对话框
            )
        )
    }

    /**
     * 处理指令响应
     * 解析设备返回的响应数据
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M50_MD_RADIO_PARAM -> {
                // 根据响应类型选择解析方式
                val result = if (cmdStr.contains("method=0"))
                // 查询响应：解析为电台参数对象
                    iotParseManager.parse<M50RadioParam>(cmdStr, IOTCommandType.M50_MD_RADIO_PARAM)
                else
                // 设置响应：解析为通用设置结果
                    iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = if (cmdStr.contains("method=0"))
                            "查询电台参数出错: ${result.message}"
                        else
                            "设置电台参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        if (cmdStr.contains("method=0")) {
                            // 查询成功：更新界面数据
                            initRadioData(result.data as M50RadioParam)
                        } else {
                            // 设置成功：检查是否还有指令需要执行
                            if (!isCommunicationExecuting()) {
                                processNavigateUp()
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }

    /**
     * 初始化电台配置数据
     * 将设备返回的参数填充到界面
     *
     * @param data 解析后的电台参数对象
     */
    private fun initRadioData(data: M50RadioParam) {
        // 解析收发频点
        frequencyOptions.firstOrNull { it.code == data.freq_group }
            ?.let { mStates.frequency.set(it.label) }
            ?: run {
                val resolved = data.freq_group.toIntOrNull()?.let { index ->
                    frequencyOptions.getOrNull(index)
                }
                mStates.frequency.set(resolved?.label ?: frequencyOptions[1].label)
            }

        // 解析发射功率
        txPowerOptions.firstOrNull { it.code == data.txpower }
            ?.let { mStates.txPower.set(it.label) }
            ?: run {
                val resolved = data.txpower.toIntOrNull()?.let { value ->
                    txPowerOptions.getOrNull(value)
                }
                mStates.txPower.set(resolved?.label ?: txPowerOptions.last().label)
            }

        // 解析空中速率
        airRateOptions.firstOrNull { it.code == data.airbaud }
            ?.let { mStates.airRate.set(it.label) }
            ?: run {
                val resolved = data.airbaud.toIntOrNull()?.let { value ->
                    airRateOptions.getOrNull(value - 1)
                }
                mStates.airRate.set(resolved?.label ?: airRateOptions[1].label)
            }

        // 解析本机地址
        if (data.local_addr.isNotEmpty()) {
            mStates.localAddress.set(data.local_addr)
        }

        // 解析目标地址
        if (data.target_addr.isNotEmpty()) {
            mStates.targetAddress.set(data.target_addr)
        }

        // 保存初始状态，用于后续修改检测
        mStates.saveInitialState()
    }

    /**
     * 从指令响应字符串中提取指定参数值
     * @param cmdStr 指令响应字符串
     * @param paramName 参数名称
     * @return 参数值，如果不存在则返回 null
     */
    private fun extractParamValue(cmdStr: String, paramName: String): String? {
        val regex = Regex("$paramName=([^&]*)")
        return regex.find(cmdStr)?.groupValues?.get(1)
    }

    /**
     * 处理返回按钮点击
     * 如果数据已修改，弹出确认对话框
     */
    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
}
