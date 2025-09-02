package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.LoraCommunicateEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.LoraCommunicateInfo
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
import com.shmedo.mcloudapp.databinding.FragmentLoraSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LoraSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoraSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLoraSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: LoraSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var loraReceiveChannelList: List<String> = emptyList()//收发频点
    private var transmitPowerList: List<String> = emptyList()//发射功率
    private var airSpeedList: List<String> = emptyList()//空中速率
    private var networkNumberList: List<String> = emptyList()//网络编号
    private var localAddressList: List<String> = emptyList()//本机地址
    private var targetAddressList: List<String> = emptyList()//目标地址

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
        binding.llToolbar.toolbar.title = "LORA配置"
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
        mStates.isTargetAddressSupport.set(productType != ProductType.COLLECTOR_G_0)

        loraReceiveChannelList =
            if (productType == ProductType.U_L_1)
                (47000..49900 step 100).map { (it.toFloat() / 100).toString() + "MHz" }
            else
                (41000..42900 step 100).map { (it.toFloat() / 100).toString() + "MHz" }

        transmitPowerList = (5..20).map { it.toString() }
        airSpeedList = (1..6).map { it.toString() }
        networkNumberList = (1..10).map { it.toString() }
        localAddressList = (1..20).map { it.toString() }
        targetAddressList = (1..20).map { it.toString() }

        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.channel.set(loraReceiveChannelList[10])
        mStates.transmitPower.set(transmitPowerList[transmitPowerList.lastIndex])//[5~20] 默认20
        mStates.airSpeed.set(airSpeedList[2])//空中速率  [1~6] 默认3
        mStates.networkNumber.set(networkNumberList[0])//网络号 [1~10] 默认1
        mStates.localAddress.set(if (productType == ProductType.COLLECTOR_G_0) localAddressList[0] else localAddressList[1])//本机地址 [1~20] 网关默认1,监测设备默认2
        mStates.targetAddress.set(if (productType == ProductType.COLLECTOR_G_0) targetAddressList[1] else targetAddressList[0])//目标地址 [1~20] 网关默认2,监测设备默认1
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val commands = mutableListOf<String>()
        val command =
            //devicetype 添加且赋值为1时，表示配置自组网网关
            if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_LORA_CTRL,
                "devicetype=1"
            )
            else IOTCommandUtil.getCommand(IOTCommandType.MD_GET_LORA_CTRL)
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }


    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

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

        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_LORA_CTRL -> {
                val result = iotParseManager.parse<LoraCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_LORA_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询信息出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_LORA_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
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

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择收发频点
         */
        fun onChannelChooseClick() {
            val selectedIndex = loraReceiveChannelList.indexOf(mStates.channel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", loraReceiveChannelList.toTypedArray(),
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
         * 选择网络编号
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