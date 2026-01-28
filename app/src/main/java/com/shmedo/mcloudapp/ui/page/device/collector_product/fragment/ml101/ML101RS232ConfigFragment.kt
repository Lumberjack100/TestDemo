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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.ml101.ML101ExtSerialParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.ML101ExtSerialParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMl101Rs232ConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ML101RS232ConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 RS232 配置页面
 *
 * 功能说明：
 * 配置 ML101 设备的 RS232 串口通信参数，包括：
 * - 开关：控制 RS232 功能的启用/禁用（0-关, 1-开）
 * - 功能选择：关闭输出/日志输出/数据透传
 * - 波特率(Bps)：9600/57600/115200
 * - 数据位：5/6/7/8
 * - 校验位：NONE/ODD/EVEN/MARK/SPACE
 * - 停止位：1/1.5/2
 *
 * 涉及指令：
 * - 查询参数: $cmd=md_cfgextserialparam&method=0
 * - 设置参数: $cmd=md_cfgextserialparam&method=1&rs232_sw=...&rs232_mode=...
 *
 * 注意事项：
 * 1. 开关值：0-关, 1-开
 * 2. 当开关关闭时，其他配置项应禁用
 * 3. 与 RS485 配置共用同一个指令类型 MD_CFG_EXT_SERIAL_PARAM
 */
class ML101RS232ConfigFragment : OptimizedBaseIOTDeviceFragment() {

    private lateinit var binding: FragmentMl101Rs232ConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: ML101RS232ConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // ==================== 选项列表定义 ====================

    /** 功能选择选项（显示文本） */
    private val functionModeDisplayList = arrayListOf("关闭输出", "日志输出", "数据透传")

    /** 功能选择选项（实际值：0-关闭输出, 1-日志输出, 2-数据透传） */
    private val functionModeValueList = arrayListOf("0", "1", "2")

    /** 波特率选项 */
    private val baudRateList = arrayListOf("9600", "57600", "115200")

    /** 数据位选项 */
    private val dataBitList = arrayListOf("5", "6", "7", "8")

    /** 校验位选项（显示文本） */
    private val parityBitDisplayList = arrayListOf("NONE", "ODD", "EVEN", "MARK", "SPACE")

    /** 校验位选项（实际值：N/O/E/M/S） */
    private val parityBitValueList = arrayListOf("N", "O", "E", "M", "S")

    /** 停止位选项 */
    private val stopBitList = arrayListOf("1", "1.5", "2")

    // ==================== 生命周期方法 ====================

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ml101_rs232_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMl101Rs232ConfigBinding
        binding.llToolbar.toolbar.title = "RS232配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
        initRefresh()
    }

    /**
     * 初始化下拉刷新
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

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    /**
     * 重置为默认参数
     * 默认值：开启, 关闭输出, 115200, 8, NONE, 1
     */
    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        mStates.functionMode.set(functionModeDisplayList[0]) // 关闭输出
        mStates.baudRate.set("115200")
        mStates.dataBit.set("8")
        mStates.parityBit.set("NONE")
        mStates.stopBit.set("1")
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    // ==================== 数据查询 ====================

    /**
     * 查询 RS232 参数
     * 发送查询指令: $cmd=md_cfgextserialparam&method=0
     * 注意：与 RS485 共用同一个查询指令，响应中包含 RS485 和 RS232 两部分参数
     */
    private fun queryData() {
        val commands = listOf(
            IOTCommandUtil.getCommand(IOTCommandType.MD_CFG_EXT_SERIAL_PARAM, "method=0")
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载对话框
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    // ==================== 数据保存 ====================

    /**
     * 保存 RS232 配置
     * 发送设置指令: $cmd=md_cfgextserialparam&method=1&rs232_sw=...
     */
    private fun saveConfiguration() {
        val commands = buildSaveCommands()

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()

        // 获取开关值：0-关, 1-开
        val swValue = if (mStates.isOpened.get()) "1" else "0"

        // 获取功能模式值
        val modeValue = getValueFromDisplayList(
            mStates.functionMode.get(),
            functionModeDisplayList,
            functionModeValueList,
            "0"
        )

        // 获取校验位值
        val parityValue = getValueFromDisplayList(
            mStates.parityBit.get(),
            parityBitDisplayList,
            parityBitValueList,
            "N"
        )

        // 创建 RS232 配置实体（使用 createRS232Entity 工厂方法）
        val entity = ML101ExtSerialParamEntity.createRS232Entity(
            sw = swValue,
            mode = modeValue,
            baud = mStates.baudRate.get(),
            paritybit = parityValue,
            dataBit = mStates.dataBit.get(),
            stopbit = mStates.stopBit.get()
        )

        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_CFG_EXT_SERIAL_PARAM,
                entity.toCommandString()
            )
        )

        return commands
    }

    /**
     * 从显示列表中获取对应的实际值
     */
    private fun getValueFromDisplayList(
        displayValue: String,
        displayList: List<String>,
        valueList: List<String>,
        defaultValue: String
    ): String {
        val index = displayList.indexOf(displayValue)
        return if (index in valueList.indices) valueList[index] else defaultValue
    }

    // ==================== 响应处理 ====================

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_CFG_EXT_SERIAL_PARAM -> {
                // 判断是查询响应还是设置响应
                if (cmdStr.contains("method=0")) {
                    handleQueryResponse(cmdStr)
                } else {
                    handleSaveResponse(cmdStr)
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理查询响应
     */
    private fun handleQueryResponse(cmdStr: String) {
        val result = iotParseManager.parse<ML101ExtSerialParamInfo>(
            cmdStr,
            IOTCommandType.MD_CFG_EXT_SERIAL_PARAM
        )
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询RS232参数出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initParamData(result.data)
            }
        }
    }

    /**
     * 处理保存响应
     */
    private fun handleSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "RS232参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) {
                    Toaster.show("保存成功")
                    // 更新初始状态
                    mStates.saveInitialState()
                }
            }
        }
    }

    // ==================== 数据初始化 ====================

    /**
     * 初始化参数数据
     * 将服务器返回的数据填充到 ViewModel 中
     * 注意：使用 RS232 相关字段（rs232_*）
     */
    private fun initParamData(data: ML101ExtSerialParamInfo) {
        try {
            // 开关：0-关, 1-开
            mStates.isOpened.set(data.isRs232Enabled())

            // 功能模式
            val modeIndex = data.rs232_mode
            if (modeIndex in functionModeDisplayList.indices) {
                mStates.functionMode.set(functionModeDisplayList[modeIndex])
            }

            // 波特率
            mStates.baudRate.set(data.rs232_baud.toString())

            // 数据位
            mStates.dataBit.set(data.rs232_dataBit.toString())

            // 校验位：需要转换
            val parityIndex = parityBitValueList.indexOf(data.rs232_paritybit.uppercase())
            if (parityIndex in parityBitDisplayList.indices) {
                mStates.parityBit.set(parityBitDisplayList[parityIndex])
            }

            // 停止位
            mStates.stopBit.set(data.rs232_stopbit)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化RS232参数数据出错")
        }
    }

    // ==================== 点击事件处理 ====================

    inner class ClickProxy : BaseClickProxy() {

        /** 选择功能模式 */
        fun onFunctionModeClick() {
            if (!mStates.isOpened.get()) return
            showBottomListPopup(
                functionModeDisplayList,
                mStates.functionMode.get()
            ) { text ->
                mStates.functionMode.set(text)
            }
        }

        /** 选择波特率 */
        fun onBaudRateClick() {
            if (!mStates.isOpened.get()) return
            showBottomListPopup(
                baudRateList,
                mStates.baudRate.get()
            ) { text ->
                mStates.baudRate.set(text)
            }
        }

        /** 选择数据位 */
        fun onDataBitClick() {
            if (!mStates.isOpened.get()) return
            showBottomListPopup(
                dataBitList,
                mStates.dataBit.get()
            ) { text ->
                mStates.dataBit.set(text)
            }
        }

        /** 选择校验位 */
        fun onParityBitClick() {
            if (!mStates.isOpened.get()) return
            showBottomListPopup(
                parityBitDisplayList,
                mStates.parityBit.get()
            ) { text ->
                mStates.parityBit.set(text)
            }
        }

        /** 选择停止位 */
        fun onStopBitClick() {
            if (!mStates.isOpened.get()) return
            showBottomListPopup(
                stopBitList,
                mStates.stopBit.get()
            ) { text ->
                mStates.stopBit.set(text)
            }
        }

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
        }

        /** 提交保存 */
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            saveConfiguration()
        }
    }

    /**
     * 显示底部列表弹窗
     */
    private fun showBottomListPopup(
        list: ArrayList<String>,
        currentValue: String,
        onSelected: (String) -> Unit
    ) {
        val selectedIndex = list.indexOf(currentValue)
        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .asBottomList(
                "", list.toTypedArray(),
                null, selectedIndex,
                { _, text -> onSelected(text) },
                0, R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
}
