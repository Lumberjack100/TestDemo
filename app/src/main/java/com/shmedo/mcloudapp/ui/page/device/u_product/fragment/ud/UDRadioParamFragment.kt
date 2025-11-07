package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RadioCommunicateEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentUdRadioParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDRadioParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/22
 * @desc: 一体式雷达水位/泥位计电台配置
 *
 */
class UDRadioParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdRadioParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDRadioParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val radioChannelNumList: List<String> =
        (451150000..470150000 step 1000000).map { it.toString() }
    private var radioChannelTextList: List<String> = emptyList()
    private val transmitPowerList: List<String> = (10..22).map { it.toString() }//发射功率
    private val airSpeedList: List<String> = (1..3).map { it.toString() }//空中速率


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_radio_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdRadioParamBinding
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
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        //451.15-470.15，1MHz步进
        radioChannelTextList = (451150..470150 step 1000).map {
            (it.toFloat() / 1000).toString()
        }
        resetDefaultParams()
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        //发送默认 10 即 461.125MHz
        mStates.sendChannel.set(radioChannelTextList.first())
        //接收默认 20 即 471.125MHz
        mStates.receiveChannel.set(radioChannelTextList.first())
        //发射功率 [10~22] 默认22
        mStates.transmitPower.set(transmitPowerList.last())
        //空中速率  [1~3] 默认1
        mStates.airSpeed.set(airSpeedList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择接收频点
         */
        fun onReceiveChannelChooseClick() {
            val selectedIndex = radioChannelTextList.indexOf(mStates.receiveChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioChannelTextList.toTypedArray(),
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
            val selectedIndex = radioChannelTextList.indexOf(mStates.sendChannel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", radioChannelTextList.toTypedArray(),
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
            rxchl = radioChannelNumList[radioChannelTextList.indexOf(mStates.receiveChannel.get())],
            txchl = radioChannelNumList[radioChannelTextList.indexOf(mStates.sendChannel.get())],
            outpwr = mStates.transmitPower.get(),
            airbaud = mStates.airSpeed.get(),
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_RADIO_CTRL)

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
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initRadioData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_RADIO_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
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

    private fun initRadioData(info: RadioCommunicateInfo) {
        try {
            mStates.isOpened.set(info.sw == "1")
            radioChannelNumList.indexOf(info.rxchl)
                .let { index ->
                    if (index in radioChannelTextList.indices) {
                        mStates.receiveChannel.set(radioChannelTextList[index])
                    }
                }
            radioChannelNumList.indexOf(info.txchl)
                .let { index ->
                    if (index in radioChannelTextList.indices) {
                        mStates.sendChannel.set(radioChannelTextList[index])
                    }
                }
            mStates.transmitPower.set(info.outpwr)
            mStates.airSpeed.set(info.airbaud)
            mStates.radioNetStatus.set(
                when (info.net_sta) {
                    "0" -> "未入网"
                    "1" -> "已入网"
                    else -> "--"
                }
            )
            mStates.parentGatewaySN.set(info.gateway_sn)

            //添加这行来保存初始状态
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