package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

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
import com.shmedo.mcloudapp.databinding.FragmentM50RadioSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50RadioSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

class M50RadioSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50RadioSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50RadioSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private data class Option(val code: String, val label: String)

    // 通讯频率选项：freq_group -> 470~508MHz，每步 2MHz
    private val frequencyOptions = (0..19).map { index ->
        val label = "${470 + index * 2}MHz"
        Option(index.toString(), label)
    }

    // 空中速率选项：1/2/3 分别对应 2.4、19.2、76.8kbps
    private val airRateOptions = listOf(
        Option("1", "2.4Kbps"),
        Option("2", "19.2Kbps"),
        Option("3", "76.8Kbps")
    )

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_radio_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50RadioSettingBinding
        binding.llToolbar.toolbar.title = "电台配置"
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
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        mStates.frequency.set(frequencyOptions.first().label)// 默认470MHz
        mStates.airRate.set(airRateOptions.first().label)// 默认2.4kbps
        mStates.localAddress.set("1")//默认本机地址
        mStates.targetAddress.set("2")//默认目标地址
    }

    inner class ClickProxy : BaseClickProxy() {
        /** 选择通讯频率 */
        fun onFrequencyChooseClick() {
            val options = frequencyOptions.map { it.label }.toTypedArray()
            val selectedIndex =
                frequencyOptions.indexOfFirst { it.label == mStates.frequency.get() }
                    .takeIf { it >= 0 } ?: 0
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "",
                    options,
                    null,
                    selectedIndex,
                    { _, text ->
                        mStates.frequency.set(text)
                    },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择空中速率 */
        fun onAirRateChooseClick() {
            val options = airRateOptions.map { it.label }.toTypedArray()
            val selectedIndex = airRateOptions.indexOfFirst { it.label == mStates.airRate.get() }
                .takeIf { it >= 0 } ?: 0
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "",
                    options,
                    null,
                    selectedIndex,
                    { _, text ->
                        mStates.airRate.set(text)
                    },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!validateInput()) {
                return
            }
            saveRadioParam()
        }
    }

    /** 校验输入参数合法性 */
    private fun validateInput(): Boolean {
        val local = mStates.localAddress.get()
        val target = mStates.targetAddress.get()

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
            showMessageDialog("本机地址取值范围为1-65535")
            return false
        }

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
            showMessageDialog("目标地址取值范围为0-65535")
            return false
        }
        if (localValue == targetValue) {
            showMessageDialog("本机地址与目标地址不能相同")
            return false
        }
        return true
    }

    private fun saveRadioParam() {
        val entity = M50RadioParamEntity(
            method = "1",
            sw = if (mStates.isOpened.get()) "1" else "0",
            freq_group = frequencyOptions.firstOrNull { it.label == mStates.frequency.get() }?.code
                ?: frequencyOptions.first().code,
            airbaud = airRateOptions.firstOrNull { it.label == mStates.airRate.get() }?.code
                ?: airRateOptions.first().code,
            local_addr = mStates.localAddress.get(),
            target_addr = mStates.targetAddress.get()
        )
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

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.M50_MD_RADIO_PARAM,
            "method=0"
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M50_MD_RADIO_PARAM -> {
                val result = if (cmdStr.contains("method=0"))
                    iotParseManager.parse<M50RadioParam>(cmdStr, IOTCommandType.M50_MD_RADIO_PARAM)
                else
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
                            initRadioData(result.data as M50RadioParam)
                        } else {
                            // 保存成功，检查是否还有指令需要执行
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

    private fun initRadioData(data: M50RadioParam) {
        mStates.isOpened.set(data.sw == "1")

        frequencyOptions.firstOrNull { it.code == data.freq_group }
            ?.let { mStates.frequency.set(it.label) }
            ?: run {
                val resolved = data.freq_group.toIntOrNull()?.let { index ->
                    frequencyOptions.getOrNull(index)
                }
                mStates.frequency.set(resolved?.label ?: frequencyOptions.first().label)
            }

        airRateOptions.firstOrNull { it.code == data.airbaud }
            ?.let { mStates.airRate.set(it.label) }
            ?: run {
                val resolved = data.airbaud.toIntOrNull()?.let { value ->
                    airRateOptions.getOrNull(value - 1)
                }
                mStates.airRate.set(resolved?.label ?: airRateOptions.first().label)
            }

        if (data.local_addr.isNotEmpty()) {
            mStates.localAddress.set(data.local_addr)
        }
        if (data.target_addr.isNotEmpty()) {
            mStates.targetAddress.set(data.target_addr)
        }

        // 保存初始状态，用于后续修改检测
        mStates.saveInitialState()
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
} 
