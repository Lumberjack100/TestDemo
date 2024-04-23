package com.shmedo.mcloudapp.device.ui.das.fragment

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentDasHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CollectorConfigModule
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SensorConfigModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.TimeCalibrationPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.CommonDeviceHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class DasHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mHeadStates: CommonDeviceHomeViewModel
    private lateinit var mCommandResponseStates: CommandResponseViewModel
    private val iotParseManager: IOTParserManager by inject()

    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private var mExitTime: Long = 0

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mHeadStates = getFragmentScopeViewModel()
        mCommandResponseStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasHomeBinding
        toolbarViewModel.toolbarIvActionVisible.set(true)
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device_warn), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device_warn), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        initModuleAdapter()
    }

    private fun initModuleAdapter() {
        binding.recyclerview.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
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
        mHeadStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isPlatformConnectionStateVisible.set(true)
                mHeadStates.platformConnectionStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
            }

            else -> {}
        }
        updateConfigModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        if (isConnected) {
            mHeadStates.isDeviceStateTagHighLight.set(true)
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
        } else {
            mHeadStates.isDeviceStateTagHighLight.set(false)
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
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
                showMessage(StringUtils.getString(R.string.disconnect_device_warn), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                }, "取消")
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }
    }

    private fun processItemClick(module: ConfigModule) {
        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.configModule) {
            is TimeCalibrationModule -> {//时间校准
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
                if (module.configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        module.configModule.navId,
                        bundle
                    )
                }
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
     * 重启设备
     */
    private fun reboot() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.REBOOT)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        if (communicateWay is BleConnect) {
            bleViewModel.launch(bleDevice!!)
        }
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
        //大于2000ms则认为是误操作，使用Toast进行提示
        if (System.currentTimeMillis() - mExitTime > 2000) {
            mExitTime= System.currentTimeMillis()
        }
        mExitTime= System.currentTimeMillis()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        Timber.e(result.message)
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

            IOTCommandType.SET_TERMINAL_TIME -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        Timber.e(result.message)
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
                    }
                }
            }

            IOTCommandType.QUERY_SAMPLE -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        Timber.e(result.message)
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

            IOTCommandType.REBOOT -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    "关于设备",
                    "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_dasHomeFragment_to_dasDeviceInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(TimeCalibrationModule())
        )
        moduleList.add(
            ConfigModule(TelemetryDataModule())
        )
        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(
            ConfigModule(CollectorConfigModule(navId = R.id.action_dasHomeFragment_to_dasCollectorSettingFragment))
        )
        moduleList.add(
            ConfigModule(DataCenterModule(navId = R.id.action_dasHomeFragment_to_dasDataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(SensorConfigModule(navId = R.id.action_dasHomeFragment_to_dasSensorHomeFragment))
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "上报方式",
                    desc = "上报规则设置",
                    resID = R.drawable.ic_device_data_center,
                    navId = R.id.action_dasHomeFragment_to_dasTerminalParameterFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_dasAdvancedSettingFragment))
        )
        binding.recyclerview.models = moduleList
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}