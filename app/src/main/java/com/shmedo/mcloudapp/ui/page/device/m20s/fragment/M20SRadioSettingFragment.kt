package com.shmedo.mcloudapp.ui.page.device.m20s.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RadioCommunicateEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM20sRadioSettingBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M20SRadioSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class M20SRadioSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM20sRadioSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: M20SRadioSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private var radioChannelList: List<String> = emptyList()
    private var transmitPowerList: List<String> = emptyList()//发射功率
    private var airSpeedList: List<String> = emptyList()//空中速率

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
        binding.llToolbar.toolbar.title = "电台配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        radioChannelList =  (45115..47015 step 100).map {
            (it.toFloat() / 100).toString() + "MHz"
        }
        transmitPowerList =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_1)
                (0..22).map { it.toString() }
            else
                (10..22).map { it.toString() }

        airSpeedList =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_1)
                (0..2).map { it.toString() }
            else
                (1..3).map { it.toString() }

        mStates.isSupportSwitch.set(productType == ProductType.GNSS_M_5)

        resetDefaultParams()
    }

    private fun resetDefaultParams() {
        when (productType) {
            ProductType.GNSS_M_1, ProductType.GNSS_M_2 -> {//M20S 载波频率以 450.15Mhz 为起始，间隔 1Mhz，进行信道划分，共划分 20 个信道
                mStates.rtcmChannel.set(radioChannelList[0])//RTCM数据频点以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 默认0
                mStates.receiveChannel.set(radioChannelList[13])//接收默认 13 即 463.15MHz
                mStates.sendChannel.set(radioChannelList[6])//发送默认 6 即 456.15MHz
                mStates.transmitPower.set(transmitPowerList.last())//发射功率 [0~22] 默认22
                mStates.airSpeed.set(airSpeedList[1])//空中速率  [0~2] 默认1
            }

            ProductType.GNSS_M_5 -> {//M50 载波频率以 450.15Mhz 为起始，间隔 1Mhz，进行信道划分，共划分 20 个信道
                mStates.isOpened.set(false)
                mStates.rtcmChannel.set(radioChannelList[0])//RTCM数据频点
                mStates.receiveChannel.set(radioChannelList[0])//接收
                mStates.sendChannel.set(radioChannelList[0])//发送
                mStates.transmitPower.set(transmitPowerList.last())//发射功率   22
                mStates.airSpeed.set(airSpeedList[0])//空中速率  1
            }

            else -> {

            }
        }
    }


    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择RTCM数据频点
         */
        fun onRTCMDataChannelChooseClick() {
            val selectedIndex = radioChannelList.indexOf(mStates.rtcmChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioChannelList.toTypedArray(),
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
            val selectedIndex = radioChannelList.indexOf(mStates.receiveChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioChannelList.toTypedArray(),
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
            val selectedIndex = radioChannelList.indexOf(mStates.sendChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioChannelList.toTypedArray(),
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
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!mStates.isOpened.get() && mStates.isSupportSwitch.get()) {
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
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            "sw=0"
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        val entity = RadioCommunicateEntity(
            sw = if (mStates.isSupportSwitch.get()) "1" else IOTConstants.NULL_KEY,
            bcchl = radioChannelList.indexOf(mStates.rtcmChannel.get()).toString(),
            rxchl = radioChannelList.indexOf(mStates.receiveChannel.get()).toString(),
            txchl = radioChannelList.indexOf(mStates.sendChannel.get()).toString(),
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
                        val errMsg = "查询电台参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "设置电台参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
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
            mStates.isOpened.set(info.sw == "1")
            info.bcchl.toInt().let {
                if (it in radioChannelList.indices) {
                    mStates.rtcmChannel.set(radioChannelList[it])
                }
            }
            info.rxchl.toInt().let {
                if (it in radioChannelList.indices) {
                    mStates.receiveChannel.set(radioChannelList[it])
                }
            }
            info.txchl.toInt().let {
                if (it in radioChannelList.indices) {
                    mStates.sendChannel.set(radioChannelList[it])
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