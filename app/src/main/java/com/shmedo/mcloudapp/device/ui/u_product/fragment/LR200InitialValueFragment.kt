package com.shmedo.mcloudapp.device.ui.u_product.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLr200InitialValueBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.LR200InitialValueViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: LR200 一体式裂缝计初始化
 *
 */
class LR200InitialValueFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200InitialValueBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: LR200InitialValueViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

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
            if (isBleDisconnected()) {
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
        commandItems.clear()
        if (mStates.isAutoInit.get()) {
            //先发送遥测指令
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.QUERY_SAMPLE
            )
            commandItems.add(command)
        } else {
            //发送初始化指令
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_LF_INITIAL_VALUE,
                "datastreams=${mStates.initValue.get()}"
            )
            commandItems.add(command)
        }
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_SAMPLE -> {//
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        //{"103_1":{"x":-1.32,"y":-4.06,"z":85.73},"203_1":0.00,"105_1":0.00}
                        //下面取出 203_1 对应的值赋值给 zeroValueMeasured：
                        val zeroValueMeasured =
                            MoshiUtil.fromJson<Map<String, Any>>(result.data)?.get("203_1")
                        //发送初始化指令
                        val command = IOTCommandUtil.getCommand(
                            IOTCommandType.MD_SET_LF_INITIAL_VALUE,
                            "datastreams=$zeroValueMeasured"
                        )
                        commandItems.add(command)
                        sendCommandFromCmdList()
                    }
                }
            }


            IOTCommandType.MD_SET_LF_INITIAL_VALUE -> {//设置
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "初始化出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始化成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}