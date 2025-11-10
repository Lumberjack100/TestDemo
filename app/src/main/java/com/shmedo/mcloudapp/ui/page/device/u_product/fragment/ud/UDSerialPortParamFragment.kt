package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UD485SerialPortEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDRainGaugeSerialPortEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UD485SerialPortInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDRainGaugeSerialPortInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentUdSerialPortParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDSerialPortParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/9/10
 * @desc: 一体式雷达水位/泥位计端口参数配置
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseIOTDeviceFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变（包括485端口配置、雨量计配置等）
 */
class UDSerialPortParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdSerialPortParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDSerialPortParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val rs485BaudRateList = arrayListOf("2400", "4800", "9600", "14400", "19200")//波特率
    private val rainGaugeResolutionList = arrayListOf("0.1", "0.2", "0.5")  //雨量计分辨率

    private var isCleanRainMode = false;

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_serial_port_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdSerialPortParamBinding
        binding.llToolbar.toolbar.title = "端口配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

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
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.rs485Enable.set(true)
        mStates.rs485BaudRate.set(rs485BaudRateList[2])//默认为 9600
        mStates.rs485Address.set("")

        mStates.rainGaugeEnable.set(true)
        mStates.rainGaugeResolution.set(rainGaugeResolutionList[0])//默认为 0.1
        mStates.rainGaugeTotalValue.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 485波特率选择
         */
        fun on485BaudRateChooseClick() {
            val selectedIndex = rs485BaudRateList.indexOf(mStates.rs485BaudRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rs485BaudRateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rs485BaudRate.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 雨量计分辨率选择
         */
        fun onRainGaugeResolutionChooseClick() {
            val selectedIndex = rainGaugeResolutionList.indexOf(mStates.rainGaugeResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rainGaugeResolutionList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rainGaugeResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 清零累计雨量值
         */
        fun onClearRainGaugeTotalValueClick() {
            val rainGaugeSerialPortEntity = UDRainGaugeSerialPortEntity(
                clean_rain = "1"
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.UD_MD_SET_RAIN_GAUGE_PARAM,
                rainGaugeSerialPortEntity.toCommandString()
            )

            isCleanRainMode = true
            sendCommandSequence(
                commands = listOf(command),
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 保存配置 - 使用新的通信架构
     */
    private fun initSaveCommand() {
        if (mStates.rs485Enable.get()) {
            if (mStates.rs485Address.get().isEmpty()) {
                showMessageDialog("请输入485地址")
                return
            }
        }

        val commands = mutableListOf<String>()

        // 设置485端口参数
        val uD485SerialPortEntity = UD485SerialPortEntity(
            sw = if (mStates.rs485Enable.get()) "1" else "0",
            baud = mStates.rs485BaudRate.get(),
            addr = mStates.rs485Address.get()
        )
        val rs485Command = IOTCommandUtil.getCommand(
            IOTCommandType.UD_MD_SET_RS485_PARAM,
            uD485SerialPortEntity.toCommandString()
        )
        commands.add(rs485Command)

        // 设置雨量计参数
        val rainGaugeSerialPortEntity = UDRainGaugeSerialPortEntity(
            sw = if (mStates.rainGaugeEnable.get()) "1" else "0",
            res = mStates.rainGaugeResolution.get()
        )
        val rainGaugeCommand = IOTCommandUtil.getCommand(
            IOTCommandType.UD_MD_SET_RAIN_GAUGE_PARAM,
            rainGaugeSerialPortEntity.toCommandString()
        )
        isCleanRainMode = false
        commands.add(rainGaugeCommand)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据 - 使用新的通信架构
     */
    private fun queryData() {
        val commands = listOf(
            IOTCommandUtil.getCommand(IOTCommandType.UD_MD_GET_RS485_PARAM),
            IOTCommandUtil.getCommand(IOTCommandType.UD_MD_GET_RAIN_GAUGE_PARAM)
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 状态查询失败显示Dialog
            )
        )
    }

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.UD_MD_GET_RS485_PARAM -> {
                val result = iotParseManager.parse<UD485SerialPortInfo>(
                    cmdStr,
                    IOTCommandType.UD_MD_GET_RS485_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询485端口参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        init485SerialPortData(result.data)
                    }
                }
            }

            IOTCommandType.UD_MD_GET_RAIN_GAUGE_PARAM -> {
                val result = iotParseManager.parse<UDRainGaugeSerialPortInfo>(
                    cmdStr,
                    IOTCommandType.UD_MD_GET_RAIN_GAUGE_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询雨量计端口参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initRainGaugeSerialPortData(result.data)
                    }
                }
            }

            IOTCommandType.UD_MD_SET_RS485_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置485端口参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            IOTCommandType.UD_MD_SET_RAIN_GAUGE_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置雨量计端口参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (isCleanRainMode) {
                            mStates.rainGaugeTotalValue.set("")
                            Toaster.show("清零成功")
                        } else {
                            if (!isCommunicationExecuting())
                                processNavigateUp()
                        }
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    private fun init485SerialPortData(uD485SerialPortInfo: UD485SerialPortInfo) {
        try {
            mStates.rs485Enable.set(uD485SerialPortInfo.sw == "1")
            mStates.rs485BaudRate.set(uD485SerialPortInfo.baud)
            mStates.rs485Address.set(uD485SerialPortInfo.addr)

            //添加这行来保存初始状态
            mStates.saveInitialState()

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initRainGaugeSerialPortData(gaugeSerialPortInfo: UDRainGaugeSerialPortInfo) {
        try {
            mStates.rainGaugeEnable.set(gaugeSerialPortInfo.sw == "1")
            mStates.rainGaugeResolution.set(gaugeSerialPortInfo.res)
            mStates.rainGaugeTotalValue.set(gaugeSerialPortInfo.total_rain)

            //添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

}