package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentLb20sAdHocNetworkSettingsBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.dialog.TimeCalibrationPopupView
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: 无线预警广播自组网设置
 *
 */
class LB20SAdHocNetworkSettingsFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLb20sAdHocNetworkSettingsBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mCommandResponseStates: CommandResponseViewModel by viewModels()

    private val iotParseManager: IOTParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lb20s_ad_hoc_network_settings,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLb20sAdHocNetworkSettingsBinding
        binding.llToolbar.toolbar.title = "自组网配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 电台设置
         */
        fun onRadioSettingsClick() {
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
                R.id.action_global_to_radioSettingsFragment,
                bundle
            )
        }

        /**
         * LoRa设置
         */
        fun onLoRaSettingsClick() {
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
                R.id.action_global_to_loraSettingFragment,
                bundle
            )
        }

        /**
         * 网关校时
         */
        fun onGatewaySchoolTimeClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            queryTerminalTime()
        }

        /**
         * 网关重启
         */
        fun onGatewayRestartClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage(
                "确定重启设备吗？",
                "温馨提示",
                "确定",
                {
                    reboot()
                },
                "取消"
            )
        }

        /**
         * 网关恢复出厂设置
         */
        fun onRestoreGatewayToFactoryClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage(
                "确定恢复出厂设置吗？",
                "温馨提示",
                "确定",
                {
                    restoreFactory()
                },
                "取消"
            )
        }
    }

    /**
     * 查询终端时间
     */
    private fun queryTerminalTime() {
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME, "devicetype=1")

        mCommandResponseStates.isResponseLoading.set(true)
        mCommandResponseStates.isResponseSuccess.set(false)
        showTimeCalibrationPopup()

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout) {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set("设备未响应")

                    } else {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(error.message)
                    }
                }
            )
        )
    }

    /**
     * 重启设备
     */
    fun reboot() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.REBOOT, "devicetype=1")

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 恢复出厂
     */
    fun restoreFactory() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.RESET, "devicetype=1")

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 显示时间校准弹窗
     */
    private fun showTimeCalibrationPopup() {
        val popupView = TimeCalibrationPopupView(requireContext())
        popupView.setTitle("时间校准", mCommandResponseStates)
            .setClickListener(object : TimeCalibrationPopupView.OnClickListener {
                override fun onSettingClick() {
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.SET_TERMINAL_TIME,
                        "time=${TimeUtils.getNowString()}&devicetype=1"
                    )

                    sendTimeCalibrationCommand(command)
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    private fun sendTimeCalibrationCommand(command: String) {
        mCommandResponseStates.isResponseLoading.set(true)
        mCommandResponseStates.isResponseSuccess.set(false)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout) {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set("设备未响应")

                    } else {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(error.message)
                    }
                }
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {//查询终端时间
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
                        if (!isCommunicationExecuting()) {
                            mCommandResponseStates.isResponseLoading.set(false)
                            mCommandResponseStates.isResponseSuccess.set(true)
                            mCommandResponseStates.isCalibratingSuccess.set(false)
                            mCommandResponseStates.deviceTime.set(result.data)
                            mCommandResponseStates.systemTime.set(TimeUtils.getNowString())
                        }
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {//设置终端时间
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            mCommandResponseStates.isResponseLoading.set(false)
                            mCommandResponseStates.isResponseSuccess.set(true)
                            mCommandResponseStates.isCalibratingSuccess.set(true)
                            mCommandResponseStates.deviceTime.set(mCommandResponseStates.systemTime.get())
                        }
                    }
                }
            }

            IOTCommandType.REBOOT -> {//重启设备
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            IOTCommandType.RESET -> {//恢复出厂设置
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reset_failed) + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                        }
                    }
                }
            }

            else -> {
            }
        }
    }


}