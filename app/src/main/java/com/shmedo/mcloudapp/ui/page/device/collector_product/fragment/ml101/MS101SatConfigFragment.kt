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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.ml101.MS101SatParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.MS101SatParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentMs101SatConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MS101SatConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：MS101 卫通配置页面
 *
 * 功能说明：
 * - 配置卫通参数（联网状态上报、休眠模式、休眠等级、溢出处理、待发数据处理等）
 * - 选择"指定删除"时，动态显示删除数据帧号输入框
 *
 * 核心业务逻辑：
 * - 卫星联网状态上报：关闭(0) / 开启(1) / 开启并上报数据帧号(2)
 * - 卫星休眠模式：不休眠(0) / 定时休眠(1) / 自动休眠(2)
 * - 卫星休眠模式等级：1-9（cpsmmode=0 时无效）
 * - 数据存储溢出处理：停止接收(0) / 循环覆盖(1)
 * - 待发数据处理：不删除(0) / 全部删除(-1) / 指定删除(1-480)
 *
 * 使用指令：IOTCommandType.MD_CFG_SAT_PARAM (md_cfgsatparam)
 * - 获取参数：method=0
 * - 设置参数：method=1
 */
class MS101SatConfigFragment : OptimizedBaseIOTDeviceFragment() {

    // ==================== 成员变量 ====================

    /** DataBinding 绑定对象 */
    private lateinit var binding: FragmentMs101SatConfigBinding

    /** 工具栏 ViewModel */
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    /** 页面状态 ViewModel */
    private val mStates: MS101SatConfigViewModel by viewModels()

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
     * 卫星联网状态上报选项
     * - 0: 关闭联网状态上报
     * - 1: 开启联网状态上报
     * - 2: 开启联网数据上报，并在数据发送成功后上报数据帧编号
     */
    private val cregModeOptions = listOf(
        Option("0", "关闭"),
        Option("1", "开启"),
        Option("2", "开启并上报数据帧号")
    )

    /**
     * 卫星休眠模式选项
     * - 0: 不休眠
     * - 1: 定时休眠
     * - 2: 自动（有待发数据时定时休眠，无待发数据时一直休眠）
     */
    private val cpsmModeOptions = listOf(
        Option("0", "不休眠"),
        Option("1", "定时休眠"),
        Option("2", "自动休眠")
    )

    /**
     * 卫星休眠模式等级选项
     * 取值范围：1-9
     * cpsmmode=0 时无效，默认值 9
     */
    private val cpsmLevelOptions = (1..9).map { level ->
        Option(level.toString(), level.toString())
    }

    /**
     * 数据存储溢出处理选项
     * - 0: 停止接收（存储满后不再接收新数据）
     * - 1: 循环覆盖（存储器满后，覆盖最早的数据）
     */
    private val svmdModeOptions = listOf(
        Option("0", "停止接收"),
        Option("1", "循环覆盖")
    )

    /**
     * 待发数据处理选项
     * - 0: 不删除
     * - -1: 全部删除
     * - 1~480: 指定删除（在此选项中，实际帧号通过输入框输入）
     *
     * 注意：选择"指定删除"时，需要在输入框中输入具体的帧号（1-480）
     */
    private val cclrModeOptions = listOf(
        Option("0", "不删除"),
        Option("-1", "全部删除"),
        Option("specify", "指定删除")  // 特殊标记，实际值由输入框决定
    )

    // ==================== 生命周期方法 ====================

    /**
     * 配置 DataBinding
     * 绑定布局、ViewModel 和点击事件代理
     */
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ms101_sat_config,
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
        binding = getBinding() as FragmentMs101SatConfigBinding
        // 设置页面标题
        binding.llToolbar.toolbar.title = "卫通配置"
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
        // 默认卫星联网状态上报：关闭 (cregmode=0)
        mStates.cregMode.set(cregModeOptions[0].label)
        // 默认卫星休眠模式：不休眠 (cpsmmode=0)
        mStates.cpsmMode.set(cpsmModeOptions[0].label)
        // 默认卫星休眠模式等级：9 (cpsmlevel=9)
        mStates.cpsmLevel.set(cpsmLevelOptions.last().label)
        // 默认数据存储溢出处理：停止接收 (svmdmode=0)
        mStates.svmdMode.set(svmdModeOptions[0].label)
        // 默认待发数据处理：不删除 (cclrmode=0)
        mStates.updateCclrMode(cclrModeOptions[0].label)
        // 默认删除帧号为空
        mStates.deleteFrameNo.set("")
    }

    // ==================== 点击事件处理 ====================

    /**
     * 点击事件代理内部类
     * 处理所有用户交互事件
     */
    inner class ClickProxy : BaseClickProxy() {

        /**
         * 卫星联网状态上报选择点击事件
         * 弹出底部选择器，选择上报模式
         */
        fun onCregModeChooseClick() {
            showBottomListPopup(
                options = cregModeOptions,
                currentValue = mStates.cregMode.get()
            ) { selectedLabel ->
                mStates.cregMode.set(selectedLabel)
            }
        }

        /**
         * 卫星休眠模式选择点击事件
         * 弹出底部选择器，选择休眠模式
         */
        fun onCpsmModeChooseClick() {
            showBottomListPopup(
                options = cpsmModeOptions,
                currentValue = mStates.cpsmMode.get()
            ) { selectedLabel ->
                mStates.cpsmMode.set(selectedLabel)
            }
        }

        /**
         * 卫星休眠模式等级选择点击事件
         * 弹出底部选择器，选择等级 1-9
         */
        fun onCpsmLevelChooseClick() {
            showBottomListPopup(
                options = cpsmLevelOptions,
                currentValue = mStates.cpsmLevel.get()
            ) { selectedLabel ->
                mStates.cpsmLevel.set(selectedLabel)
            }
        }

        /**
         * 数据存储溢出处理选择点击事件
         * 弹出底部选择器，选择处理方式
         */
        fun onSvmdModeChooseClick() {
            showBottomListPopup(
                options = svmdModeOptions,
                currentValue = mStates.svmdMode.get()
            ) { selectedLabel ->
                mStates.svmdMode.set(selectedLabel)
            }
        }

        /**
         * 待发数据处理选择点击事件
         * 弹出底部选择器，选择处理方式
         * 选择"指定删除"时会自动显示删除帧号输入框
         */
        fun onCclrModeChooseClick() {
            showBottomListPopup(
                options = cclrModeOptions,
                currentValue = mStates.cclrMode.get()
            ) { selectedLabel ->
                mStates.updateCclrMode(selectedLabel)
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
            saveSatParam()
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
        // 校验删除帧号（仅在"指定删除"模式下需要校验）
        if (mStates.isDeleteFrameNoVisible.value == true) {
            val frameNo = mStates.deleteFrameNo.get()

            if (frameNo.isBlank()) {
                showMessageDialog("请输入删除数据帧号")
                return false
            }
            if (!frameNo.all { it.isDigit() }) {
                showMessageDialog("删除数据帧号仅支持数字")
                return false
            }
            val frameNoValue = frameNo.toIntOrNull() ?: run {
                showMessageDialog("删除数据帧号格式不正确")
                return false
            }
            if (frameNoValue !in 1..480) {
                showMessageDialog("删除数据帧号取值范围为 1-480")
                return false
            }
        }

        return true
    }

    // ==================== 通信方法 ====================

    /**
     * 保存卫通配置参数
     * 构建设置指令并发送到设备
     */
    private fun saveSatParam() {
        // 获取当前选项的代码值（用于发送指令）
        val cregmodeCode = cregModeOptions.firstOrNull { it.label == mStates.cregMode.get() }?.code
            ?: cregModeOptions[0].code
        val cpsmmodeCode = cpsmModeOptions.firstOrNull { it.label == mStates.cpsmMode.get() }?.code
            ?: cpsmModeOptions[0].code
        val cpsmlevelCode = cpsmLevelOptions.firstOrNull { it.label == mStates.cpsmLevel.get() }?.code
            ?: cpsmLevelOptions.last().code
        val svmdmodeCode = svmdModeOptions.firstOrNull { it.label == mStates.svmdMode.get() }?.code
            ?: svmdModeOptions[0].code

        // 计算 cclrmode 的值
        // - 不删除: 0
        // - 全部删除: -1
        // - 指定删除: 1~480（使用输入框的值）
        val cclrmodeCode = when (mStates.cclrMode.get()) {
            "不删除" -> "0"
            "全部删除" -> "-1"
            "指定删除" -> mStates.deleteFrameNo.get()
            else -> "0"
        }

        // 构建卫通参数配置实体
        val entity = MS101SatParamEntity(
            method = "1",  // 1: 设置参数
            cregmode = cregmodeCode,
            cpsmmode = cpsmmodeCode,
            cpsmlevel = cpsmlevelCode,
            svmdmode = svmdmodeCode,
            cclrmode = cclrmodeCode
        )

        // 发送指令
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_CFG_SAT_PARAM,
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
     * 查询当前卫通配置
     * 发送获取参数指令（method=0）
     */
    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_CFG_SAT_PARAM,
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
            IOTCommandType.MD_CFG_SAT_PARAM -> {
                // 根据响应类型选择解析方式
                val result = if (cmdStr.contains("method=0"))
                // 查询响应：解析为卫通参数对象
                    iotParseManager.parse<MS101SatParamInfo>(cmdStr, IOTCommandType.MD_CFG_SAT_PARAM)
                else
                // 设置响应：解析为通用设置结果
                    iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = if (cmdStr.contains("method=0"))
                            "查询卫通参数出错: ${result.message}"
                        else
                            "设置卫通参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        if (cmdStr.contains("method=0")) {
                            // 查询成功：更新界面数据
                            initSatData(result.data as MS101SatParamInfo)
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
     * 初始化卫通配置数据
     * 将设备返回的参数填充到界面
     *
     * @param data 解析后的卫通参数对象
     */
    private fun initSatData(data: MS101SatParamInfo) {
        // 解析卫星联网状态上报模式
        cregModeOptions.firstOrNull { it.code == data.cregmode.toString() }
            ?.let { mStates.cregMode.set(it.label) }
            ?: run { mStates.cregMode.set(cregModeOptions[0].label) }

        // 解析卫星休眠模式
        cpsmModeOptions.firstOrNull { it.code == data.cpsmmode.toString() }
            ?.let { mStates.cpsmMode.set(it.label) }
            ?: run { mStates.cpsmMode.set(cpsmModeOptions[0].label) }

        // 解析卫星休眠模式等级
        cpsmLevelOptions.firstOrNull { it.code == data.cpsmlevel.toString() }
            ?.let { mStates.cpsmLevel.set(it.label) }
            ?: run { mStates.cpsmLevel.set(cpsmLevelOptions.last().label) }

        // 解析数据存储溢出处理模式
        svmdModeOptions.firstOrNull { it.code == data.svmdmode.toString() }
            ?.let { mStates.svmdMode.set(it.label) }
            ?: run { mStates.svmdMode.set(svmdModeOptions[0].label) }

        // 解析待发数据处理模式
        when {
            data.cclrmode == 0 -> {
                // 不删除
                mStates.updateCclrMode("不删除")
                mStates.deleteFrameNo.set("")
            }

            data.cclrmode == -1 -> {
                // 全部删除
                mStates.updateCclrMode("全部删除")
                mStates.deleteFrameNo.set("")
            }

            data.cclrmode in 1..480 -> {
                // 指定删除
                mStates.updateCclrMode("指定删除")
                mStates.deleteFrameNo.set(data.cclrmode.toString())
            }

            else -> {
                // 默认不删除
                mStates.updateCclrMode("不删除")
                mStates.deleteFrameNo.set("")
            }
        }

        // 保存初始状态，用于后续修改检测
        mStates.saveInitialState()
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
