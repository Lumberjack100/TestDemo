package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lr200

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
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
import com.shmedo.mcloudapp.databinding.FragmentLr200InitialValueBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LR200InitialValueViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: LR200 一体式裂缝计初始化
 *
 */
class LR200InitialValueFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200InitialValueBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: LR200InitialValueViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var initialValueLoadingDialogId = ""

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lr200_initial_value,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLr200InitialValueBinding
        binding.llToolbar.toolbar.title = "初始化"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onModeCheckedChanged(view: View) {
            when (view.id) {
                R.id.auto_init -> {
                    mStates.isAutoInit.set(true)
                }

                R.id.manual_init -> {
                    mStates.isAutoInit.set(false)
                }
            }
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!mStates.isAutoInit.get() && mStates.initValue.get().isEmpty()) {
                showMessageDialog("请输入初始值!")
                return
            }
            showMessage(
                "确定初始化设备？",
                "温馨提示",
                "确定",
                {
                    initSaveCommand()
                },
                "取消"
            )
        }
    }

    private fun initSaveCommand() {
        val command = if (mStates.isAutoInit.get()) {
            //先发送遥测指令
            IOTCommandUtil.getCommand(IOTCommandType.SAMPLE)
        } else {
            //发送初始化指令
            IOTCommandUtil.getCommand(
                IOTCommandType.LF_MD_MANUAL_SET_INITIAL_VALUE,
                "datastreams=${mStates.initValue.get()}"
            )
        }

        initialValueLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示特殊的加载对话框了
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {//
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(initialValueLoadingDialogId)
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        //{"103_1":{"x":-1.32,"y":-4.06,"z":85.73},"203_1":0.00,"105_1":0.00}
                        //下面取出 203_1 对应的值赋值给 zeroValueMeasured：
                        val zeroValueMeasured =
                            MoshiUtil.fromJson<Map<String, Any>>(result.data)?.get("203_1")
                        //发送初始化指令
                        val command = IOTCommandUtil.getCommand(
                            IOTCommandType.LF_MD_MANUAL_SET_INITIAL_VALUE,
                            "datastreams=$zeroValueMeasured"
                        )
                        sendCommandSequence(
                            commands = listOf(command),
                            config = CommandSequenceConfig(
                                showLoadingDialog = false, // 已经显示特殊的加载对话框了
                                errorConfig = ErrorConfig.dialogConfig()
                            )
                        )
                    }
                }
            }


            IOTCommandType.LF_MD_MANUAL_SET_INITIAL_VALUE -> {//设置
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(initialValueLoadingDialogId)
                        val errMsg = "初始化出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        dismissLoadingDialog(initialValueLoadingDialogId)
                        if (!isCommunicationExecuting())
                            processNavigateUp("初始化成功")
                    }
                }
            }

            else -> {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}