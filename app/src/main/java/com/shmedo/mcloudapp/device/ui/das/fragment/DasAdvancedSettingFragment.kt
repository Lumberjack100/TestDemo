package com.shmedo.mcloudapp.device.ui.das.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentDasAdvancedSettingBinding
import com.shmedo.mcloudapp.device.common.BaseDasAdvancedSettingClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class DasAdvancedSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_advanced_setting,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasAdvancedSettingBinding
        binding.llToolbar.toolbar.title = "设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseDasAdvancedSettingClickProxy() {
        override fun onFirmWareSelectClick() {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(R.id.action_global_to_firmwareUpgradeFragment, bundle)
        }

        override fun onResetClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command = IOTCommandUtil.getCommand(IOTCommandType.RESET)
                commandItems.add(command)
                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        override fun onAudibleAlarmClick() {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(R.id.action_dasAdvancedSettingFragment_to_dasAudibleAlarmFragment, bundle)
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.RESET -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = StringUtils.getString(R.string.reset_failed) + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                        }
                    }
                }
            }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}