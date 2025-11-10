package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRWiredNetEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRWirelessNetEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRWiredNet
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRWirelessNet
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702NetworkCommunicationBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702NetworkConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：    遥测终端机网络配置页面
 */
class MR702NetworkConfigFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702NetworkCommunicationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702NetworkConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val ipModeList by lazy { Utils.getApp().resources.getStringArray(R.array.ip_mode) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_network_communication, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702NetworkCommunicationBinding
        binding.llToolbar.toolbar.title = "网络配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        //4G 交互方式下不允许操作
        mStates.isWirelessDisabled.set(communicateWay is NetPlatformConnect)

        mStates.isWirelessOpened.set(true)
        mStates.apnName.set("")
        mStates.userName.set("")
        mStates.pwd.set("")

        mStates.isEthernetOpened.set(true)
        mStates.ipMode.set(ipModeList[1])
        mStates.isManualVisible.set(false)
        mStates.ip.set("")
        mStates.subnetMask.set("")
        mStates.gateway.set("")
        mStates.preferredDNS.set("")
        mStates.alternateDNS.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onIPModeChooseClick() {
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
                        mStates.isManualVisible.set(text == "手动")
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.isWirelessOpened.get()) {
            val wirelessNetEntity = MRWirelessNetEntity(
                switch = "1",
                apn = mStates.apnName.get().ifEmpty { IOTConstants.NULL_KEY },
                username = mStates.userName.get().ifEmpty { IOTConstants.NULL_KEY },
                password = mStates.pwd.get().ifEmpty { IOTConstants.NULL_KEY }
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_DATA_NETWORK,
                wirelessNetEntity.toCommandString()
            )
            commands.add(command)
        } else {
            val wirelessNetEntity = MRWirelessNetEntity(switch = "0")
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_DATA_NETWORK,
                wirelessNetEntity.toCommandString()
            )
            commands.add(command)
        }


        //手动模式
        if (mStates.isEthernetOpened.get()) {
            if (mStates.isManualVisible.get()) {
                if (!RegexUtils.isIP(mStates.ip.get())) {
                    showMessageDialog("请输入有效的IP地址!")
                    return
                }
                if (!RegexUtils.isIP(mStates.subnetMask.get())) {
                    showMessageDialog("请输入有效的子网掩码!")
                    return
                }
                if (!RegexUtils.isIP(mStates.gateway.get())) {
                    showMessageDialog("请输入有效的网关!")
                    return
                }
                if (!RegexUtils.isIP(mStates.preferredDNS.get())) {
                    showMessageDialog("请输入有效的首选DNS!")
                    return
                }
            }
            val wiredNetEntity = MRWiredNetEntity(
                switch = "1",
                dhcp = ipModeList.toList().indexOf(mStates.ipMode.get()).toString(),
                ipaddr = mStates.ip.get(),
                mask = mStates.subnetMask.get(),
                gateway = mStates.gateway.get(),
                dns = mStates.preferredDNS.get()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_WIRED_NETWORK,
                wiredNetEntity.toCommandString()
            )
            commands.add(command)
        } else {
            val wiredNetEntity = MRWiredNetEntity(switch = "0")
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_WIRED_NETWORK,
                wiredNetEntity.toCommandString()
            )
            commands.add(command)
        }

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val commands = mutableListOf<String>()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DATA_NETWORK)
        commands.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_WIRED_NETWORK)
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DATA_NETWORK -> {
                val result = iotParseManager.parse<MRWirelessNet>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DATA_NETWORK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询移动网络配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initWirelessData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_WIRED_NETWORK -> {
                val result = iotParseManager.parse<MRWiredNet>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_WIRED_NETWORK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询以太网络配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initWiredData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_DATA_NETWORK -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "移动网络配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_SET_WIRED_NETWORK -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "以太网络配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {
            }
        }
    }

    private fun initWirelessData(mrWirelessNet: MRWirelessNet) {
        try {
            mStates.isWirelessOpened.set(mrWirelessNet.switch == "1")
            mStates.apnName.set(mrWirelessNet.apn)
            mStates.userName.set(mrWirelessNet.username)
            mStates.pwd.set(mrWirelessNet.password)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initWiredData(mrWiredNet: MRWiredNet) {
        try {
            mStates.isEthernetOpened.set(mrWiredNet.switch == "1")
            mrWiredNet.dhcp.toIntOrNull()?.let {
                if (it in ipModeList.indices) {
                    mStates.ipMode.set(ipModeList[it])
                }
            }
            mStates.isManualVisible.set(mrWiredNet.dhcp == "0")
            mStates.ip.set(mrWiredNet.ipaddr)
            mStates.subnetMask.set(mrWiredNet.mask)
            mStates.gateway.set(mrWiredNet.gateway)
            mStates.preferredDNS.set(mrWiredNet.dns)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

}