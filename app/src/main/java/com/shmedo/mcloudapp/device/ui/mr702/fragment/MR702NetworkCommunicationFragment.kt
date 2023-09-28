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
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRWiredNetEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRWirelessNetEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWiredNet
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWirelessNet
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
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
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702NetworkCommunicationViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   网络与通信页面
 */
class MR702NetworkCommunicationFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMR702NetworkCommunicationBinding by lazy { getBinding() as FragmentMR702NetworkCommunicationBinding }
    private val mStates: MR702NetworkCommunicationViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

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

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
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
                            closeWireless()
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
                            closeEthernet()
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
            initSaveCommand()
        }
    }

    private fun closeWireless() {
        commandItems.clear()
        val wirelessNetEntity = MRWirelessNetEntity(
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_DATA_NETWORK,
            wirelessNetEntity.toCommandString()
        )
        commandItems.add(command)
        sendCommandFromCmdList()
    }

    private fun closeEthernet() {
        commandItems.clear()
        val wiredNetEntity = MRWiredNetEntity(
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_WIRED_NETWORK,
            wiredNetEntity.toCommandString()
        )
        commandItems.add(command)
        sendCommandFromCmdList()
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
                switch = "1",
                apn = mStates.apnName.get().ifEmpty { IOTConstants.NULL_KEY },
                username = mStates.userName.get().ifEmpty { IOTConstants.NULL_KEY },
                password = mStates.pwd.get().ifEmpty { IOTConstants.NULL_KEY }
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_SET_DATA_NETWORK,
                wirelessNetEntity.toCommandString()
            )
            commandItems.add(command)
        }

        //手动模式
        if (mStates.isEthernetOpened.get()) {
            if (mStates.isManualVisible.get()) {
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
            }

            val wiredNetEntity = MRWiredNetEntity(
                switch = "1",
                dhcp = if (mStates.isManualVisible.get()) "0" else "1",
                ipaddr = mStates.ip.get(),
                mask = mStates.subnetMask.get(),
                gateway = mStates.gateway.get(),
                dns = mStates.preferredDNS.get(),
                dnss = mStates.alternateDNS.get()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_SET_WIRED_NETWORK,
                wiredNetEntity.toCommandString()
            )
            commandItems.add(command)
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_NETWORK)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_WIRED_NETWORK)
        commandItems.add(command)

        sendCommandFromCmdList()
        showLoadingDialog(StringUtils.getString(R.string.loading))
        if (communicateWay is BleConnect) {
            startTimeoutJob()
        }
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
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

            IOTCommandType.MD_MR_SET_DATA_NETWORK -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "无线配置出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }
                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                            setEditable(false)
                        }
                    }
                }
            }

            IOTCommandType.MD_MR_SET_WIRED_NETWORK -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "以太网配置出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }
                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                            setEditable(false)
                        }
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

}