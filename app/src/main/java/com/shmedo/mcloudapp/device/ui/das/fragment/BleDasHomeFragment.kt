package com.shmedo.mcloudapp.device.ui.das.fragment

import android.os.Bundle
import android.widget.CompoundButton
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.md_cmd.assemble.entity.das.AuthenticationEntity
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.model.das.AuthenticationInfo
import com.shmedo.lib.device.base.md_cmd.model.das.AuthenticationResultInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.DesUtil
import com.shmedo.lib.device.base.md_cmd.utils.HexUtils
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentBleDasHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CollectorConfigModule
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SensorConfigModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.TimeCalibrationPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.BleDasHomeFragmentViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.nio.charset.StandardCharsets

class BleDasHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mHeadStates: BleDasHomeFragmentViewModel
    private lateinit var mCommandResponseStates: CommandResponseViewModel
    private val mdParseManager: MDParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mHeadStates = getFragmentScopeViewModel()
        mCommandResponseStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_das_home, BR.stateVM, mHeadStates)
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
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
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

        mHeadStates.isDeviceStateTagHighLight.set(false)
        mHeadStates.deviceStateTagText.set("未连接")
        mHeadStates.isConnectOperateVisible.set(true)
        mHeadStates.connectOperateText.set("蓝牙连接")
        mHeadStates.isPlatformConnectionStateVisible.set(true)
        mHeadStates.platformConnectionStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")

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
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                }, "取消")
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mHeadStates.isActived.set(isChecked)
            if (!isChecked) {
//                disableDigitalPiezometer()
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
//                if (module.configModule.navId != 0) {
//                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
//                        communicateWay,
//                        deviceInfo,
//                        bleDevice
//                    )
//                    nav().navigate(
//                        module.configModule.navId,
//                        bundle
//                    )
//                }

                setAuthenticateWay()
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
        commandItems.add("\r\n" + command)

        Timber.d("设置认证类型指令===%s", command)
        sendMDCommandFromCmdList(isStartTimeoutJob = false)
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
                val command = "##222,${deviceInfo.deviceToken},0,${strEncrypt.uppercase()}\r\n"
                Timber.d("设备登录验证指令===%s", command)

                commandItems.clear()
                commandItems.add(command)
                sendMDCommandFromCmdList(isStartTimeoutJob = false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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