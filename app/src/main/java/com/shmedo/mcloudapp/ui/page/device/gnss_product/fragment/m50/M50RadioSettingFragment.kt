package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RadioCommunicateEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentM50RadioSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50RadioSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

class M50RadioSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50RadioSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50RadioSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var rtcmChannelList: List<String> = emptyList()
    private var alarmChannelList: List<String> = emptyList()//报警频点
    private var localAddressList: List<String> = emptyList()//本机地址


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_radio_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50RadioSettingBinding
        binding.llToolbar.toolbar.title = "电台配置"
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
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        rtcmChannelList = (48241..49041 step 200).map { (it.toFloat() / 100).toString() + "MHz" }
        alarmChannelList = (47041..48041 step 200).map { (it.toFloat() / 100).toString() + "MHz" }
        localAddressList = (1100..1140 step 2).map { it.toString() }

        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        mStates.rtcmChannel.set(rtcmChannelList[2])//RTCM数据频点 默认为486.41MHz
        mStates.alarmChannel.set(alarmChannelList.last())//报警频点 默认为480.41MHz
        mStates.localAddress.set(localAddressList.first())//本机地址  默认为1100
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择RTCM数据频点
         */
        fun onRTCMDataChannelChooseClick() {
            val selectedIndex = rtcmChannelList.indexOf(mStates.rtcmChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rtcmChannelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rtcmChannel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }


        /**
         * 选择报警频点
         */
        fun onAlarmChannelChooseClick() {
            val selectedIndex = alarmChannelList.indexOf(mStates.alarmChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", alarmChannelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.alarmChannel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 本机地址
         */
        fun onLocalAddressChooseClick() {
            val selectedIndex = localAddressList.indexOf(mStates.localAddress.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", localAddressList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.localAddress.set(text)
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
            if (!mStates.isOpened.get()) {
                disableRadio()
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 关闭
     */
    private fun disableRadio() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            "sw=0"
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    private fun initSaveCommand() {
        val entity = RadioCommunicateEntity(
            sw = "1",
            bcchl = rtcmChannelList.indexOf(mStates.rtcmChannel.get()).toString(),
            txchl = alarmChannelList.indexOf(mStates.alarmChannel.get()).toString(),
            localaddr = localAddressList.indexOf(mStates.localAddress.get()).toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            entity.toCommandString()
        )
        sendCommandSequence(
            commands = listOf(command),
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
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_RADIO_CTRL
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_RADIO_CTRL -> {
                val result = iotParseManager.parse<RadioCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_RADIO_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询电台参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initRadioData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_RADIO_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置电台参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {
            }
        }
    }

    private fun initRadioData(data: RadioCommunicateInfo) {
        mStates.isOpened.set(data.sw == "1")

        // 设置RTCM频点
        if (data.bcchl.isNotEmpty()) {
            val index = data.bcchl.toIntOrNull() ?: 0
            if (index in rtcmChannelList.indices) {
                mStates.rtcmChannel.set(rtcmChannelList[index])
            }
        }

        // 设置报警频点
        if (data.txchl.isNotEmpty()) {
            val index = data.txchl.toIntOrNull() ?: 0
            if (index in alarmChannelList.indices) {
                mStates.alarmChannel.set(alarmChannelList[index])
            }
        }

        // 设置本机地址
        if (data.localaddr.isNotEmpty()) {
            val index = data.localaddr.toIntOrNull() ?: 0
            if (index in localAddressList.indices) {
                mStates.localAddress.set(localAddressList[index])
            }
        }

        // 保存初始状态，用于后续修改检测
        mStates.saveInitialState()
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
} 