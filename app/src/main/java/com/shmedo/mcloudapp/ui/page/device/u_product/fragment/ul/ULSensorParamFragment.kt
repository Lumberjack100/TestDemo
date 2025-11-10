package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ul

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
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentUlSensorParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ULSensorParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/8/6
 * @desc: 北斗林木生长监测终端传感配置
 *
 */
class ULSensorParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUlSensorParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: ULSensorParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // 配置选项列表
    private val initializationList = arrayListOf("是", "否")
    private val initializationModeList = arrayListOf("手动", "自动")


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ul_sensor_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUlSensorParamBinding
        binding.llToolbar.toolbar.title = "传感配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
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
        mStates.initialization.set(initializationList[1]) // 默认否
        mStates.initializationMode.set(initializationModeList[1]) // 默认自动
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据
     */
    private fun queryData() {
        val commands = listOf(
            IOTCommandUtil.getCommand(IOTCommandType.LF_MD_GET_INITIAL_VALUE),
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    /**
     * 保存配置
     */
    private fun saveConfiguration() {
        // 验证输入数据
        if (!validateInputData()) {
            return
        }

        // 构建保存指令序列
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
     * 验证输入数据
     */
    private fun validateInputData(): Boolean {
        if (mStates.initialization.get() == "是" && mStates.initializationMode.get() == "手动") {
            if (mStates.manualValue.get().isEmpty()) {
                showMessageDialog("请输入手动初始值!")
                return false
            }

            // 验证输入格式
            try {
                mStates.manualValue.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的手动初始值!")
                return false
            }
        }

        return true
    }

    /**
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()
        if (mStates.initialization.get() == "是") {
            val command = if (mStates.initializationMode.get() == "自动")
                IOTCommandUtil.getCommand(IOTCommandType.LF_MD_AUTO_SET_INITIAL_VALUE)
            else
                IOTCommandUtil.getCommand(
                    IOTCommandType.LF_MD_MANUAL_SET_INITIAL_VALUE,
                    "datastreams=${mStates.manualValue.get()}"
                )

            commands.add(command)
        }

        return commands
    }

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.LF_MD_GET_INITIAL_VALUE -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.LF_MD_GET_INITIAL_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        mStates.manualValue.set(result.data)
                    }
                }
            }

            IOTCommandType.LF_MD_MANUAL_SET_INITIAL_VALUE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            IOTCommandType.LF_MD_AUTO_SET_INITIAL_VALUE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {
        /** 选择是否初始化 */
        fun onInitializationChooseClick() {
            val selectedIndex =
                initializationList.indexOf(mStates.initialization.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initialization.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择初始化模式 */
        fun onInitializationModeChooseClick() {
            val selectedIndex = initializationModeList.indexOf(mStates.initializationMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
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

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

}