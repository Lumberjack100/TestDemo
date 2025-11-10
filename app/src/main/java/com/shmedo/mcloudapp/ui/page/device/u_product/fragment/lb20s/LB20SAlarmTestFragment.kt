package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.BroadcastEntity
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
import com.shmedo.mcloudapp.databinding.FragmentLb20sAlarmTestBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LB20SAlarmTestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: 无线预警广播报警测试
 *
 */
class LB20SAlarmTestFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLb20sAlarmTestBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: LB20SAlarmTestViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()



    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lb20s_alarm_test,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLb20sAlarmTestBinding
        binding.llToolbar.toolbar.title = "报警测试"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 一键报警测试
         */
        fun onOnClickAlarmTestClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage(
                "是否发送报警语音测试？",
                "温馨提示",
                "确定",
                {
                    initSaveCommand()
                },
                "取消"
            )
        }

        /**
         * 自定义播报
         */
        fun onCustomBroadcastClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_lB20SCustomAlarmTestFragment,
                bundle
            )
        }
    }

    private fun initSaveCommand() {
        val entity = BroadcastEntity(
            b_num = "3",
            b_size = "99",
            b_content = "报警测试，请勿慌张"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.BROADCAST,
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

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.BROADCAST -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "播报出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("播报成功")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }


}