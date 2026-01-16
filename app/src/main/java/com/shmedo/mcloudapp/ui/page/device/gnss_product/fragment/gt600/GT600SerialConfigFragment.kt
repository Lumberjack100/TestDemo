package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.MdSensorConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.SerialPortConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.DualAntennaData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.MdSensorData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.SerialPortData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentGt600SerialConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.GT600SerialConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 串口配置页面
 *
 * 页面包含串口参数分组：
 * - 波特率: 9600, 19200, 38400, 57600, 115200
 * - 功能选择: 传感器采集, NMEA输出, 差分数据输入, 差分数据输出, 原始数据输出, 解算结果输出
 * - 功能开关: 开/关（暂无对应指令字段，仅展示逻辑）
 *
 * 涉及指令：
 * - 查询参数: md_getdbguart
 * - 设置参数: md_setdbguart
 *
 * 注意：
 * 1. 功能开关目前暂没有对应的指令字段，页面上先实现展示逻辑，不触发实际的设置
 * 2. 当功能选择为"传感器采集"时显示"功能开关"项，其他情况隐藏
 */
class GT600SerialConfigFragment : OptimizedBaseIOTDeviceFragment() {

    private lateinit var binding: FragmentGt600SerialConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: GT600SerialConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // ========== 选项列表定义 ==========

    /** 波特率选项（显示文本） */
    private val baudRateDisplayList = arrayListOf("9600", "19200", "38400", "57600", "115200")

    /** 波特率选项（实际值，与显示相同） */
    private val baudRateValueList = arrayListOf("9600", "19200", "38400", "57600", "115200")

    /** 功能选择选项（显示文本） */
    private val functionTypeDisplayList = arrayListOf(
        "传感器采集",
        "NMEA输出",
        "差分数据输入",
        "差分数据输出",
        "原始数据输出",
        "解算结果输出"
    )

    /** 功能选择选项（实际值） */
    private val functionTypeValueList = arrayListOf("1", "2", "3", "4", "5", "6")

    /** 功能开关选项（显示文本） */
    private val functionSwitchDisplayList = arrayListOf("开", "关")

    // ========== 生命周期方法 ==========

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_gt600_serial_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600SerialConfigBinding
        binding.llToolbar.toolbar.title = "串口配置"
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
     */
    private fun resetDefaultParams() {
        // 串口参数默认值
        mStates.baudRate.set(baudRateDisplayList[0]) // 默认 9600
        mStates.functionType.set(functionTypeDisplayList[0]) // 默认 传感器采集
        mStates.functionSwitch.set(functionSwitchDisplayList[0]) // 默认 开
        // 默认"传感器采集"时显示功能开关
        updateFunctionSwitchVisibility(functionTypeDisplayList[0])
    }

    /**
     * 更新功能开关的可见性
     * 当功能选择为"传感器采集"时显示，其他情况隐藏
     */
    private fun updateFunctionSwitchVisibility(functionType: String) {
        mStates.isFunctionSwitchVisible.set(functionType == "传感器采集")
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    // ========== 数据查询 ==========

    /**
     * 查询串口参数
     * 发送查询指令
     */
    private fun queryData() {
        val commands = listOf(
            // 查询串口参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DB_GUART),
            // 查询功能开关参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_SENSOR, "method=0")
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

    // ========== 数据保存 ==========

    /**
     * 保存配置
     * 发送设置指令
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

        // 串口参数设置指令
        val typeValue = getValueFromDisplayList(
            mStates.functionType.get(),
            functionTypeDisplayList,
            functionTypeValueList,
            "1"
        )
        val serialPortEntity = SerialPortConfigEntity(
            type = typeValue,
            baud = getValueFromDisplayList(
                mStates.baudRate.get(),
                baudRateDisplayList,
                baudRateValueList,
                "9600"
            )
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_DB_GUART,
                serialPortEntity.toCommandString()
            )
        )

        // 功能开关设置指令（仅当功能选择为"传感器采集"时）
        if (typeValue == "1") {
            val switchValue = if (mStates.functionSwitch.get() == "开") "1" else "0"
            val sensorSwitchEntity = MdSensorConfigEntity(
                method = "1",
                switch = switchValue
            )
            commands.add(
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_SENSOR,
                    sensorSwitchEntity.toCommandString()
                )
            )
        }

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

    // ========== 响应处理 ==========

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DB_GUART -> {
                handleSerialPortQueryResponse(cmdStr)
            }

            IOTCommandType.MD_SET_DB_GUART -> {
                handleSerialPortSaveResponse(cmdStr)
            }

            IOTCommandType.MD_SENSOR -> {
                handleSensorSwitchResponse(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理串口参数查询响应
     */
    private fun handleSerialPortQueryResponse(cmdStr: String) {
        val result =
            iotParseManager.parse<SerialPortData>(cmdStr, IOTCommandType.MD_GET_DB_GUART)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询串口参数出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initSerialPortData(result.data)
            }
        }
    }

    /**
     * 处理串口参数保存响应
     */
    private fun handleSerialPortSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "串口参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    /**
     * 处理功能开关响应（查询 or 设置）
     */
    private fun handleSensorSwitchResponse(cmdStr: String) {
        val result = if (cmdStr.contains("method=0"))
            iotParseManager.parse<MdSensorData>(cmdStr, IOTCommandType.MD_SENSOR)
        else
            iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = if (cmdStr.contains("method=0"))
                    "查询功能开关参数出错: ${result.message}"
                else
                    "功能开关参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                if (cmdStr.contains("method=0")) {
                    initSensorSwitchData(result.data as MdSensorData)
                } else {
                    // 保存成功，检查是否还有指令需要执行
                    if (!isCommunicationExecuting()) {
                        processNavigateUp()
                    }
                }
            }
        }
    }

    /**
     * 处理功能开关设置响应
     */
    private fun handleSensorSwitchSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "功能开关参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }
            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    // ========== 数据初始化 ==========

    /**
     * 初始化串口参数数据
     */
    private fun initSerialPortData(data: SerialPortData) {
        try {
            // 波特率
            val baudIndex = baudRateValueList.indexOf(data.baud)
            if (baudIndex in baudRateDisplayList.indices) {
                mStates.baudRate.set(baudRateDisplayList[baudIndex])
            }

            // 功能类型
            val typeIndex = functionTypeValueList.indexOf(data.type)
            if (typeIndex in functionTypeDisplayList.indices) {
                val functionType = functionTypeDisplayList[typeIndex]
                mStates.functionType.set(functionType)
                // 更新功能开关可见性
                updateFunctionSwitchVisibility(functionType)
            }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化串口参数数据出错")
        }
    }

    /**
     * 初始化功能开关数据
     */
    private fun initSensorSwitchData(data: MdSensorData) {
        try {
            val switchText = if (data.switch == "1") "开" else "关"
            mStates.functionSwitch.set(switchText)

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化功能开关数据出错")
        }
    }

    // ========== 点击事件处理 ==========

    inner class ClickProxy : BaseClickProxy() {

        /** 选择波特率 */
        fun onBaudRateSelected() {
            showBottomListPopup(
                baudRateDisplayList,
                mStates.baudRate.get()
            ) { text ->
                mStates.baudRate.set(text)
            }
        }

        /** 选择功能类型 */
        fun onFunctionTypeSelected() {
            showBottomListPopup(
                functionTypeDisplayList,
                mStates.functionType.get()
            ) { text ->
                mStates.functionType.set(text)
                // 更新功能开关可见性：仅当"传感器采集"时显示
                updateFunctionSwitchVisibility(text)
            }
        }

        /** 选择功能开关 */
        fun onFunctionSwitchSelected() {
            showBottomListPopup(
                functionSwitchDisplayList,
                mStates.functionSwitch.get()
            ) { text ->
                mStates.functionSwitch.set(text)
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
