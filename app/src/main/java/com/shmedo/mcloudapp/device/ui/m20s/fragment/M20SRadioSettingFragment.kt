package com.shmedo.mcloudapp.device.ui.m20s.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.RadioCommunicateEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentM20sRadioSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.M20SRadioSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

class M20SRadioSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM20sRadioSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: M20SRadioSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val radioReceiveChannelList by lazy { Utils.getApp().resources.getStringArray(R.array.radio_channel) }
    private val transmitPowerList: List<String> = (0..22).map { it.toString() }
    private val airSpeedList: List<String> = (0..2).map { it.toString() }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m20s_radio_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20sRadioSettingBinding
        binding.llToolbar.toolbar.title = "电台设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        resetParams()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择RTCM数据频点
         */
        fun onRTCMDataChannelChooseClick() {
            val selectedIndex = radioReceiveChannelList.indexOf(mStates.rtcmChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioReceiveChannelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.rtcmChannel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择接收频点
         */
        fun onReceiveChannelChooseClick() {
            val selectedIndex = radioReceiveChannelList.indexOf(mStates.receiveChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioReceiveChannelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.receiveChannel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择发送频点
         */
        fun onSendChannelChooseClick() {
            val selectedIndex = radioReceiveChannelList.indexOf(mStates.sendChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioReceiveChannelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.sendChannel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择发射功率
         */
        fun onTransmitPowerChooseClick() {
            val selectedIndex = transmitPowerList.indexOf(mStates.transmitPower.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", transmitPowerList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.transmitPower.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择空中速率
         */
        fun onAirSpeedChooseClick() {
            val selectedIndex = airSpeedList.indexOf(mStates.airSpeed.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", airSpeedList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.airSpeed.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun resetParams() {
        mStates.rtcmChannel.set(radioReceiveChannelList[0])//RTCM数据频点以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 默认0
        mStates.receiveChannel.set(radioReceiveChannelList[13])//载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 自组网网关：接收默认6，发送默认13 M20S：接收默认13，发送默认6
        mStates.sendChannel.set(radioReceiveChannelList[6])//自组网网关：接收默认6，发送默认13
        mStates.transmitPower.set(transmitPowerList[transmitPowerList.lastIndex])//发射功率 [0~22] 默认22
        mStates.airSpeed.set(airSpeedList[1])//空中速率  [0~2] 默认1
    }

    private fun initSaveCommand() {
        commandItems.clear()
        val entity = RadioCommunicateEntity(
            bcchl = radioReceiveChannelList.indexOf(mStates.rtcmChannel.get()).toString(),
            rxchl = radioReceiveChannelList.indexOf(mStates.receiveChannel.get()).toString(),
            txchl = radioReceiveChannelList.indexOf(mStates.sendChannel.get()).toString(),
            outpwr = mStates.transmitPower.get(),
            airbaud = mStates.airSpeed.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_RADIO_CTRL
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_RADIO_CTRL -> {
                val result = iotParseManager.parse<RadioCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_RADIO_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询电台参数出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initRadioData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_RADIO_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置电台参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initRadioData(info: RadioCommunicateInfo) {
        try {
            info.bcchl.toInt().let {
                if (it in radioReceiveChannelList.indices) {
                    mStates.rtcmChannel.set(radioReceiveChannelList[it])
                }
            }
            info.rxchl.toInt().let {
                if (it in radioReceiveChannelList.indices) {
                    mStates.receiveChannel.set(radioReceiveChannelList[it])
                }
            }
            info.txchl.toInt().let {
                if (it in radioReceiveChannelList.indices) {
                    mStates.sendChannel.set(radioReceiveChannelList[it])
                }
            }
            mStates.transmitPower.set(info.outpwr)
            mStates.airSpeed.set(info.airbaud)

        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}