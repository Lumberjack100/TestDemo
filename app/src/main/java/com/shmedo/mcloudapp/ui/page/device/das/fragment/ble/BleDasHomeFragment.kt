package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble

import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.PopTip
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.lib.cmd.base.md_cmd.assemble.entity.das.AuthenticationEntity
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDLowEnergyModel
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceTimeInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.AuthenticationInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.AuthenticationResultInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.DasBaseConfigInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.DesUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.HexUtils
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentBleDasHomeBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CollectorConfigModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.TelemetryDataModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BleDasHomeFragmentViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TimeCalibrationPopupView
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.nio.charset.StandardCharsets

class BleDasHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleDasHomeFragmentViewModel
    private lateinit var mCommandResponseStates: CommandResponseViewModel
    private val mdParseManager: MDParserManager by inject()

    private var isDoSetTimeCmd = false

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        mCommandResponseStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_das_home, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleDasHomeBinding
        toolbarViewModel.toolbarIvActionVisible.set(true)
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
        mStates.productLightResId.set(R.drawable.device_logo_qingxieyi)
        mStates.productGrayResId.set(R.drawable.device_logo_qingxieyi_gray)
        mStates.productLogoResId.set(mStates.productLightResId.get())
        mStates.productName.set(deviceInfo.productName)
        mStates.deviceToken.set(deviceInfo.deviceToken)
        mStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken }.replace("BHY-RDS","BHY-3S"))
        mStates.firmwareVersion.set(deviceInfo.firmwareVersion)

        mStates.isDeviceStateTagHighLight.set(false)
        mStates.deviceStateTagText.set("未连接")
        mStates.isConnectOperateVisible.set(true)
        mStates.connectOperateText.set("蓝牙连接")
        mStates.isIOTPlatformStateVisible.set(true)
        mStates.iotPlatformStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")

        updateConfigModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mStates.isConnected.set(isConnected)
        mStates.isDeviceStateTagHighLight.set(isConnected)
        if (isConnected) {
            mStates.deviceStateTagText.set("已连接")
            mStates.connectOperateText.set("断开连接")
            mStates.productLogoResId.set(mStates.productLightResId.get())
        } else {
            mStates.deviceStateTagText.set("未连接")
            mStates.connectOperateText.set("蓝牙连接")
            mStates.productLogoResId.set(mStates.productGrayResId.get())
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            if (isChecked) {
                mStates.isActivated.set(true)
                //开启/关闭设备低功耗模式
                setLowEnergyModel(true)
                return
            }
            showMessage(StringUtils.getString(R.string.disactive_device_warn), "温馨提示", "确定", {
                mStates.isActivated.set(false)
                setLowEnergyModel(false)
            }, "取消", {
                (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
            })
        }
    }

    private fun processItemClick(module: ConfigModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.functionModule) {
            is TimeCalibrationModule -> {//时间校准
                isDoSetTimeCmd = false
                mCommandResponseStates.isResponseLoading.set(true)
                queryTerminalTime()
                showTimeCalibrationPopup()
            }

            is TelemetryDataModule -> {//召测
                doTelemetryCmd()
            }

            is RebootModule -> {//重启设备
                showMessage("确定重启设备吗？", "温馨提示", "确定", {
                    reboot()
                }, "取消")
            }

            is CollectorConfigModule -> {//采集器配置
                if (module.functionModule.navId != 0) {
                    val bundle = BleDasCollectorSettingFragment.newBundleArguments(
                        mStates.collectorModel.get(),
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice,
                    )
                    nav().navigate(
                        module.functionModule.navId,
                        bundle
                    )
                }
            }

            is SensorConfigModule -> {//传感器配置
                if (module.functionModule.navId != 0) {
                    val bundle = BleDasSensorHomeFragment.newBundleArguments(
                        mStates.collectorModel.get(),
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice,
                    )
                    nav().navigate(
                        module.functionModule.navId,
                        bundle
                    )
                }
            }

            else -> {
                if (module.functionModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        module.functionModule.navId,
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
                    isDoSetTimeCmd = true
                    commandItems.clear()
                    val command =
                        MDCommandUtil.getCommand(
                            MDCommandType.LOCAL_TIME, TimeUtils.getNowString(
                                TimeUtils.getSafeDateFormat("yyMMddHHmmss")
                            )
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

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
    }

    override fun lazyLoadData() {
        if (communicateWay is BleConnect) {
            bleViewModel.launch(bleDevice!!)
        }
    }

    override fun onBleDeviceReady() {
//        super.onBleDeviceReady()
        setAuthenticateWay()
    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    private fun setAuthenticateWay() {
        commandItems.clear()
        val entity = AuthenticationEntity(deviceInfo.deviceToken, "0")
        val command =
            MDCommandUtil.getCommand(MDCommandType.AUTHENTICATION_CONFIG, entity.toCommandString())
        commandItems.add(command)

        Timber.d("设置认证类型指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }

    /**
     * 开始认证流程
     */
    private fun sendAuthenticateCodeCmd(authenticateParam: String) {
        Timber.d("解密前:%s", authenticateParam)
        val resultData = HexUtils.hexStringToBytes(authenticateParam)
        try {
            val deskey = "12345678"
            // 解密后认证码
            val strDecrypt = String(DesUtil.decrypt(resultData, deskey)!!, StandardCharsets.UTF_8)
            Timber.d("解密后:%s", strDecrypt)

            if (strDecrypt.isNotEmpty()) {
                // 反转6位随机码
                val reverseRandomCode = strDecrypt.substring(0, 6).reversed()
                val byteEncrypt =
                    DesUtil.encrypt(reverseRandomCode.toByteArray() + deskey.toByteArray(), deskey)
                // 加密后认证码
                val strEncrypt = HexUtils.bytesToHexString(byteEncrypt!!)!!
                val command =
                    "##222,${deviceInfo.deviceToken},0,${strEncrypt.uppercase()}${MDConstants.COMMAND_FOOTER}"
                Timber.d("设备登录验证指令===%s", command)

                commandItems.clear()
                commandItems.add(command)
                sendCommandFromCmdList(isStartTimeoutJob = false)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    private fun queryDASConfigInfo() {
        commandItems.clear()

        val command =
            MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
        commandItems.add(command)

        Timber.d("获取基础配置信息指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }

    /**
     * 打开/关闭设备低功耗模式
     */
    private fun setLowEnergyModel(isActivate: Boolean) {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.LOW_ENERGY,
            if (isActivate) MDLowEnergyModel.ACTIVATE.toString() else MDLowEnergyModel.STANDBY.toString()
        )
        commandItems.add(command)
        Timber.d("打开/关闭设备低功耗模式指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }

    private fun queryTerminalTime() {
        commandItems.clear()

        val command =
            MDCommandUtil.getCommand(MDCommandType.LOCAL_TIME)
        commandItems.add(command)

        Timber.d("获取设备时间信息指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun doTelemetryCmd() {
        commandItems.clear()

        val command =
            MDCommandUtil.getCommand(MDCommandType.INSTANT_COLLEACTOR)
        commandItems.add(command)

        Timber.d("遥测设备指令===%s", command)
        showLoadingDialog(StringUtils.getString(R.string.cmd_dispatch_loading_tip))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 重启设备
     */
    private fun reboot() {
        commandItems.clear()
        val command =
            MDCommandUtil.getCommand(
                MDCommandType.REBOOT_DEVICE,
                "1"
            )
        commandItems.add(command)

        Timber.d("发送保存配置重启设备指令===%s", command)
        showLoadingDialog(StringUtils.getString(R.string.cmd_dispatch_loading_tip))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }


    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(cmdStr, isDismissLoadingDialog, false, msg)
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.LOCAL_TIME,
            -> {
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

        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.AUTHENTICATION_CONFIG -> {
                val result = mdParseManager.parse<AuthenticationInfo>(
                    cmdStr,
                    MDCommandType.AUTHENTICATION_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        setAuthenticateWay()//重新认证
                        return
                    }

                    is MDCommandResult.Success -> {
                        val authenticationInfo: AuthenticationInfo = result.data
                        sendAuthenticateCodeCmd(authenticationInfo.publicKey)
                    }
                }
            }

            MDCommandType.DAS_SEND_AUTHENTICATION_RESULT -> {
                val result = mdParseManager.parse<AuthenticationResultInfo>(
                    cmdStr,
                    MDCommandType.DAS_SEND_AUTHENTICATION_RESULT
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        setAuthenticateWay()//重新认证
                        return
                    }

                    is MDCommandResult.Success -> {
                        val authenticationInfo: AuthenticationResultInfo = result.data
                        if (authenticationInfo.result == "1") {
                            onAuthenticateResult(true)
                        } else {
                            Toaster.show("设备认证失败!")
                            bleViewModel.disconnect()
                            onAuthenticateResult(false)
                        }
                    }
                }
            }

            MDCommandType.BASE_CONFIG -> {
                val result = mdParseManager.parse<DasBaseConfigInfo>(
                    cmdStr,
                    MDCommandType.BASE_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基础配置信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initBaseConfigInfo(result.data)
                    }
                }
            }

            MDCommandType.LOW_ENERGY -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "激活/待机出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("激活成功")
                        }
                    }
                }
            }

            MDCommandType.LOCAL_TIME -> {
                val result = mdParseManager.parse<DeviceTimeInfo>(
                    cmdStr,
                    MDCommandType.LOCAL_TIME
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询/设置终端时间出错"
                        Timber.e("$errMsg: ${result.message}")
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        mCommandResponseStates.isCalibratingSuccess.set(isDoSetTimeCmd)
                        if (!isDoSetTimeCmd) {
                            mCommandResponseStates.deviceTime.set(result.data.time)
                            mCommandResponseStates.systemTime.set(TimeUtils.getNowString())
                        } else {
                            mCommandResponseStates.deviceTime.set(mCommandResponseStates.systemTime.get())
                        }
                    }
                }
            }

            MDCommandType.INSTANT_COLLEACTOR -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "遥测出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            PopTip.show("遥测成功!").setMarginBottom(ConvertUtils.dp2px(300f))
                                .autoDismiss(2000).iconSuccess()
                        }
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "$cmdStr 指令出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            if (cmdStr.contains("0191"))
                                Toaster.show("设备即将重启!")
                        }
                    }
                }
            }

            MDCommandType.REBOOT_DEVICE -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "重启出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("设备即将重启!")
                        }
                    }
                }
            }

            else -> {
                if (cmdStr.contains("Please verify the equipment.")) {
                    Toaster.show("设备认证失败!")
                    bleViewModel.disconnect()
                    onAuthenticateResult(false)

                } else if (cmdStr.contains("Equipment Verify OK.")) {
                    onAuthenticateResult(true)

                } else {

                }
            }
        }
    }

    private fun onAuthenticateResult(isSuccess: Boolean) {
        cancelNearbyCommunicationTimeoutJob()
        if (isSuccess) {
            queryDASConfigInfo()
        }
    }

    private fun initBaseConfigInfo(info: DasBaseConfigInfo) {
        try {
            mStates.collectorModel.set(info.collectorModel)
            mStates.isActivated.set(MDLowEnergyModel.value(info.activeStatus) == MDLowEnergyModel.ACTIVATE)
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
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
                    navId = R.id.action_global_to_commonRunningDeviceInfoFragment
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
            ConfigModule(CollectorConfigModule(navId = R.id.action_bleDasHomeFragment_to_bleDasCollectorSettingFragment))
        )
        moduleList.add(
            ConfigModule(DataCenterModule(navId = R.id.action_bleDasHomeFragment_to_bleDasDataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(SensorConfigModule(navId = R.id.action_bleDasHomeFragment_to_bleDasSensorHomeFragment))
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_bleDasAdvancedSettingFragment))
        )
        binding.rvModule.models = moduleList
    }

    // 设置心跳检查
    private fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(com.shmedo.core.commonlib.utils.AppContants.Communication.DELAY_10000_MILLIS)  // 30秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime = TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    Timber.d("startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                    // 仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mStates.isConnected.get() && isNearbyCommunicationTimeout(lastUpdateTime)) {
                        Timber.d("bingo startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = MDCommandUtil.getCommand(MDCommandType.HEART_BEAT)
                        Timber.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}