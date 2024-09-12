package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
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
import com.shmedo.mcloudapp.databinding.FragmentUniversalDeviceHomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.TelemetryDataModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TimeCalibrationPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonDeviceHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

abstract class UniversalDeviceHomeFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentUniversalDeviceHomeBinding
    protected lateinit var toolbarViewModel: ToolbarViewModel
    protected lateinit var mHeadStates: CommonDeviceHomeViewModel
    protected lateinit var mCommandResponseStates: CommandResponseViewModel
    protected val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mHeadStates = getFragmentScopeViewModel()
        mCommandResponseStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_universal_device_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDeviceHomeBinding
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        initModuleAdapter()
    }

    private fun initModuleAdapter() {
        binding.rvModule.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<ConfigModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val module = getModel<ConfigModule>()
                processItemClick(module)
            }
        }
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        val deviceName =
            if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken }
        mHeadStates.deviceName.set(deviceName.replace("BHY-RDS", "BHY-3S"))
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion.ifEmpty { AppContants.PLACE_HOLDER_VALUE })
        mHeadStates.isRunningStateVisible.set(false)
        mHeadStates.isPlatformListVisible.set(false)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isIOTPlatformStateVisible.set(true)
                mHeadStates.iotPlatformStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
            }

            else -> {}
        }
        updateConfigModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        mHeadStates.isDeviceStateTagHighLight.set(isConnected)
        if (isConnected) {
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
            mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        } else {
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
            mHeadStates.productLogoResId.set(mHeadStates.productGrayResId.get())
        }
        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModule) {
                it.functionModule.refreshStatus(isConnected)
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            val bundle = QueryDeviceDataFragment.newBundleArguments(
                deviceInfo.deviceToken
            )
            nav(binding.llToolbar.ivAction).navigate(
                R.id.action_global_to_queryDeviceDataFragment, bundle
            )
        }

        override fun onConnectOperateClick() {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                    },
                    "取消"
                )
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }

        override fun onAdmeModeChooseClick() {
            chooseAdmeMode()
        }
    }

    private fun processItemClick(module: ConfigModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.functionModule) {
            is TimeCalibrationModule -> {//时间校准
                queryTerminalTime()
            }

            is TelemetryDataModule -> {//召测
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE)
                commandItems.add(command)

                if (communicateWay is BleConnect) {
                    mCommandResponseStates.isResponseLoading.set(true)
                    showTelemetryDataPopup()
                }
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is RebootModule -> {//重启设备
                showMessage("确定重启设备吗？", "温馨提示", "确定", {
                    reboot()
                }, "取消")
            }

            else -> {
                processOtherItemClick(module.functionModule)
            }
        }
    }

    /**
     * 显示时间校准弹窗
     */
    private fun showTimeCalibrationPopup() {
        val popupView = TimeCalibrationPopupView(requireContext())
        popupView.setTitle("时间校准", mCommandResponseStates)
            .setClickListener(object : TimeCalibrationPopupView.OnClickListener {
                override fun onSettingClick() {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.SET_TERMINAL_TIME,
                        "time=${TimeUtils.getNowString()}"
                    )
                    commandItems.add(command)
                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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

    /**
     * 显示遥测数据弹窗
     */
    private fun showTelemetryDataPopup() {
        val popupView = TelemetryPopupView(requireContext())
        popupView.setTitle("召测", mCommandResponseStates)
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    /**
     * 查询终端时间
     */
    private fun queryTerminalTime() {
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)
        commandItems.add(command)

        if (communicateWay is BleConnect) {
            mCommandResponseStates.isResponseLoading.set(true)
            showTimeCalibrationPopup()
        }
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            onNetPlatformReady()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    protected open fun onNetPlatformReady() {

    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        super.doNetDispatchSuccess(cmdStr)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
                showTimeCalibrationPopup()
            }

            IOTCommandType.SET_TERMINAL_TIME -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
                showTelemetryDataPopup()
            }

            else -> {}
        }
    }

    override fun doCmdResponseResultError(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set(errorMsg)
            }

            else -> {
                super.doCmdResponseResultError(cmdStr, errorMsg)
            }
        }
    }

    override fun doCmdResponseResultTimeOut(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            else -> {
                super.doCmdResponseResultTimeOut(cmdStr, errorMsg)
            }
        }
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(cmdStr, isDismissLoadingDialog, false, msg)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            else -> {

            }
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {//查询终端时间
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)

                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        mCommandResponseStates.isCalibratingSuccess.set(false)
                        mCommandResponseStates.deviceTime.set(result.data)
                        mCommandResponseStates.systemTime.set(TimeUtils.getNowString())
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {//设置终端时间
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)

                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        mCommandResponseStates.isCalibratingSuccess.set(true)
                        mCommandResponseStates.deviceTime.set(mCommandResponseStates.systemTime.get())
                    }
                }
            }

            IOTCommandType.QUERY_SAMPLE -> {//召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)

                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        try {
                            mCommandResponseStates.responseContent.set(result.data)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            IOTCommandType.REBOOT -> {//重启设备
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {
                processOtherCmdResult(
                    IOTCommandUtil.extractCommandType(cmdStr),
                    cmdStr
                )
            }
        }
    }

    protected abstract fun updateConfigModuleData()

    protected open fun chooseAdmeMode() {}

    protected open fun processOtherItemClick(configModule: DeviceFunctionModule) {
        if (configModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(
                configModule.navId,
                bundle
            )
        }
    }

    protected open fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {}

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}