package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
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
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.LoraCommunicateEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.common.LoraCommunicateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLoraSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.LoraSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoraSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLoraSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: LoraSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val loraReceiveChannelList by lazy { Utils.getApp().resources.getStringArray(R.array.lora_channel) }
    private val transmitPowerList: List<String> = (5..20).map { it.toString() }
    private val airSpeedList: List<String> = (1..6).map { it.toString() }
    private val networkNumberList: List<String> = (1..10).map { it.toString() }
    private val localAddressList: List<String> = (1..20).map { it.toString() }
    private val targetAddressList: List<String> = (1..20).map { it.toString() }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lora_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLoraSettingBinding
        binding.llToolbar.toolbar.title = "LORA设置"
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
        mStates.isTargetAddressSupport.set(productType != ProductType.COLLECTOR_G_0)
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
         * 选择收发频率
         */
        fun onChannelChooseClick() {
            val selectedIndex = loraReceiveChannelList.indexOf(mStates.channel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", loraReceiveChannelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.channel.set(text)
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
         * 选择网络号
         */
        fun onNetworkNumberChooseClick() {
            val selectedIndex = networkNumberList.indexOf(mStates.networkNumber.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", networkNumberList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.networkNumber.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择本机地址
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
         * 选择目标地址
         */
        fun onTargetAddressChooseClick() {
            val selectedIndex = targetAddressList.indexOf(mStates.targetAddress.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", targetAddressList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.targetAddress.set(text)
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
        mStates.channel.set(loraReceiveChannelList[10])//信道 [0~19] 载波频率以410Mhz为起始，间隔1Mhz，进行信道划分，共划分30个信道，默认10
        mStates.transmitPower.set(transmitPowerList[transmitPowerList.lastIndex])//[5~20] 默认20
        mStates.airSpeed.set(airSpeedList[2])//空中速率  [1~6] 默认3
        mStates.networkNumber.set(networkNumberList[0])//网络号 [1~10] 默认1
        mStates.localAddress.set(if (productType == ProductType.COLLECTOR_G_0) localAddressList[0] else localAddressList[1])//本机地址 [1~20] 网关默认1,监测设备默认2
        mStates.targetAddress.set(if (productType == ProductType.COLLECTOR_G_0) targetAddressList[1] else targetAddressList[0])//目标地址 [1~20] 网关默认2,监测设备默认1
    }

    private fun initSaveCommand() {
        commandItems.clear()
        val entity = LoraCommunicateEntity(
            chl = loraReceiveChannelList.indexOf(mStates.channel.get()).toString(),
            outpwr = mStates.transmitPower.get(),
            airbaud = mStates.airSpeed.get(),
            netid = mStates.networkNumber.get(),
            localid = mStates.localAddress.get(),
            dstid = if (mStates.isTargetAddressSupport.get()) mStates.targetAddress.get() else IOTConstants.NULL_KEY
        )
        //devicetype  添加且赋值为1时，表示配置自组网网关
        val command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_LORA_CTRL,
            "${entity.toCommandString()}&devicetype=1"
        ) else IOTCommandUtil.getCommand(IOTCommandType.MD_SET_LORA_CTRL, entity.toCommandString())
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command =
            //devicetype  添加且赋值为1时，表示配置自组网网关
            if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_LORA_CTRL,
                "devicetype=1"
            )
            else IOTCommandUtil.getCommand(IOTCommandType.MD_GET_LORA_CTRL)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_LORA_CTRL -> {
                val result = iotParseManager.parse<LoraCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_LORA_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_LORA_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
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

            else -> {}
        }
    }

    private fun initParamData(info: LoraCommunicateInfo) {
        try {
            mStates.channel.set(
                if (info.chl.toInt() in loraReceiveChannelList.indices) {
                    loraReceiveChannelList[info.chl.toInt()]
                } else {
                    loraReceiveChannelList[10]
                }
            )
            mStates.transmitPower.set(info.outpwr)
            mStates.airSpeed.set(info.airbaud)
            mStates.networkNumber.set(info.netid)
            mStates.localAddress.set(info.localid)
            mStates.targetAddress.set(info.dstid)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}