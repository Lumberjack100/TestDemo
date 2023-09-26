package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.entity.mr.MRWiredNetEntity
import com.shmedo.lib.device.base.iot_cmd.entity.mr.MRWirelessNetEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWiredNet
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWirelessNet
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentMR702NetworkCommunicationBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702NetworkCommunicationViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702NetworkCommunicationFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMR702NetworkCommunicationBinding by lazy { getBinding() as FragmentMR702NetworkCommunicationBinding }
    private val mStates: MR702NetworkCommunicationViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParseManager by inject()

    private val ipModeList by lazy { Utils.getApp().resources.getStringArray(R.array.ip_mode) }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m_r702_network_communication, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "网络与通信"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
        //4G 交互方式下不允许操作
        mStates.isWirelessDisabled.set(communicateWay is NetPlatformConnect)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            toolbarViewModel.toolbarIvActionVisible.set(false)
            toolbarViewModel.toolbarTvActionVisible.set(true)
            mStates.isEditable.set(true)
        }

        override fun onToolbarTvClick() {
            toolbarViewModel.toolbarIvActionVisible.set(true)
            toolbarViewModel.toolbarTvActionVisible.set(false)
            mStates.isEditable.set(false)
        }

        fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.mobileCommunicationSB -> { //4G通信
                    mStates.isWirelessOpened.set(isChecked)
                    if (!isChecked) {
                        showMessage("确定关闭4G接入吗？", "温馨提示", "确定", {

                        }, "取消", {
                            mStates.isWirelessOpened.set(true)
                            (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                        })
                    }
                }

                R.id.ethernetAccessSB -> { //以太网接入
                    mStates.isEthernetOpened.set(isChecked)
                    if (!isChecked) {
                        showMessage("确定关闭以太网接入吗？", "温馨提示", "确定", {

                        }, "取消", {
                            mStates.isEthernetOpened.set(true)
                            (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                        })
                    }
                }
            }
        }

        fun onIPModeSwitchClick() {
            val selectedIndex = ipModeList.indexOf(mStates.ipMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择分配方式", ipModeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.ipMode.set(text)
                        mStates.isManualVisible.set(position == 0)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!checkValueIsValid()) return
            initSaveCommand()
        }
    }

    private fun checkValueIsValid(): Boolean {
        if (mStates.isWirelessOpened.get()) {
            if (mStates.apnName.get().isEmpty()) {
                Toaster.show("请输入APN名称")
                return false
            }
            if (mStates.userName.get().isEmpty()) {
                Toaster.show("请输入用户名")
                return false
            }
            if (mStates.pwd.get().isEmpty()) {
                Toaster.show("请输入密码")
                return false
            }
        }
        //手动模式
        if (mStates.isEthernetOpened.get() && mStates.isManualVisible.get()) {
            if (!RegexUtils.isIP(mStates.ip.get())) {
                Toaster.show("请输入有效的IP地址")
                return false
            }
            if (!RegexUtils.isIP(mStates.subnetMask.get())) {
                Toaster.show("请输入有效的子网掩码")
                return false
            }
            if (!RegexUtils.isIP(mStates.gateway.get())) {
                Toaster.show("请输入有效的网关")
                return false
            }
            if (!RegexUtils.isIP(mStates.preferredDNS.get())) {
                Toaster.show("请输入有效的首选DNS")
                return false
            }
            if (!RegexUtils.isIP(mStates.alternateDNS.get())) {
                Toaster.show("请输入有效的备用DNS")
                return false
            }
        }

        return true
    }

    private fun initSaveCommand() {
        commandItems.clear()

        if (mStates.isWirelessOpened.get()) {
//            if (mStates.apnName.get().isEmpty()) {
//                Toaster.show("请输入APN名称")
//                return
//            }
//            if (mStates.userName.get().isEmpty()) {
//                Toaster.show("请输入用户名")
//                return
//            }
//            if (mStates.pwd.get().isEmpty()) {
//                Toaster.show("请输入密码")
//                return
//            }
            val wirelessNetEntity = MRWirelessNetEntity(
                switch = if (binding.mobileCommunicationSB.isChecked) "1" else "0",
                apn = mStates.apnName.get(),
                username = mStates.userName.get(),
                password = mStates.pwd.get()
            )
            commandItems.add(wirelessNetEntity.toCommandString())
        }

        //手动模式
        if (mStates.isEthernetOpened.get() && mStates.isManualVisible.get()) {
            if (!RegexUtils.isIP(mStates.ip.get())) {
                Toaster.show("请输入有效的IP地址")
                return
            }
            if (!RegexUtils.isIP(mStates.subnetMask.get())) {
                Toaster.show("请输入有效的子网掩码")
                return
            }
            if (!RegexUtils.isIP(mStates.gateway.get())) {
                Toaster.show("请输入有效的网关")
                return
            }
            if (!RegexUtils.isIP(mStates.preferredDNS.get())) {
                Toaster.show("请输入有效的首选DNS")
                return
            }
            if (!RegexUtils.isIP(mStates.alternateDNS.get())) {
                Toaster.show("请输入有效的备用DNS")
                return
            }

            val wiredNetEntity = MRWiredNetEntity(
                switch = if (binding.ethernetAccessSB.isChecked) "1" else "0",
                dhcp = if (mStates.isManualVisible.get()) "0" else "1",
                ipaddr = mStates.ip.get(),
                mask = mStates.subnetMask.get(),
                gateway = mStates.gateway.get(),
                dns = mStates.preferredDNS.get(),
                dnss = mStates.alternateDNS.get()
            )
            commandItems.add(wiredNetEntity.toCommandString())
        }

        sendCommandFromCmdList()
        showLoadingDialog(StringUtils.getString(R.string.processing))
        if (communicateWay is BleConnect) {
            startTimeoutJob(AppContants.Communication.DELAY_10000_MILLIS)
        }
    }

    override fun lazyLoadData() {
        queryData()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandManager.getCommand(IOTCommandType.MD_MR_GET_DATA_NETWORK)
        commandItems.add(command)

        command = IOTCommandManager.getCommand(IOTCommandType.MD_MR_GET_WIRED_NETWORK)
        commandItems.add(command)

        sendCommandFromCmdList()
        showLoadingDialog(StringUtils.getString(R.string.loading))
        if (communicateWay is BleConnect) {
            startTimeoutJob()
        }
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> Toaster.show("下发指令失败: $errorMsg")

            else -> {}
        }
    }

    override fun cancelTimeoutJob() {
        super.cancelTimeoutJob()
        dismissLoadingDialog()
    }

    override fun showTimeoutAlert() {
        // 关闭 loading 框并显示超时警告
        dismissLoadingDialog()
        Toaster.show("发送指令超时,请稍后尝试")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                val result = iotParseManager.parse<MRWirelessNet>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_NETWORK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询无线配置出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initWirelessData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_WIRED_NETWORK -> {
                val result = iotParseManager.parse<MRWiredNet>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_WIRED_NETWORK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询以太网配置出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {}
                        initWiredData(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initWirelessData(mrWirelessNet: MRWirelessNet) {
        mStates.isWirelessOpened.set(mrWirelessNet.switch == "1")
        binding.mobileCommunicationSB.setCheckedImmediatelyNoEvent(mrWirelessNet.switch == "1")
        mStates.apnName.set(mrWirelessNet.apn)
        mStates.userName.set(mrWirelessNet.username)
        mStates.pwd.set(mrWirelessNet.password)
    }

    private fun initWiredData(mrWiredNet: MRWiredNet) {
        mStates.isEthernetOpened.set(mrWiredNet.switch == "1")
        binding.ethernetAccessSB.setCheckedImmediatelyNoEvent(mrWiredNet.switch == "1")
        mStates.ipMode.set(ipModeList[if (mrWiredNet.dhcp == "0") 0 else 1])
        mStates.isManualVisible.set(mrWiredNet.dhcp == "0")
        mStates.ip.set(mrWiredNet.ipaddr)
        mStates.subnetMask.set(mrWiredNet.mask)
        mStates.gateway.set(mrWiredNet.gateway)
        mStates.preferredDNS.set(mrWiredNet.dns)
        mStates.alternateDNS.set(mrWiredNet.dnss)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}