package com.shmedo.mcloudapp.device.ui.das.fragment.ble

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentDasAdvancedSettingBinding
import com.shmedo.mcloudapp.device.common.BaseDasAdvancedSettingClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
class BleDasAdvancedSettingFragment : BaseMDDeviceFragment() {
    private lateinit var binding: FragmentDasAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private val mdParseManager: MDParserManager by inject()


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

        override fun onSyncLocationClick() {

        }
        override fun onCommandDebugClick() {
            val bundle = BaseMDDeviceFragment.newBundleArguments(
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(R.id.action_global_to_bleMDCommandLogPrintFragment, bundle)
        }

        override fun onResetClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command = MDCommandUtil.getCommand(MDCommandType.RESTORE_FACTORY_SETTING)
                commandItems.add(command)
                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendMDCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        override fun onRemoteDebuggingClick() {

        }
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.RESTORE_FACTORY_SETTING -> {
                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = StringUtils.getString(R.string.reset_failed) + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList {
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