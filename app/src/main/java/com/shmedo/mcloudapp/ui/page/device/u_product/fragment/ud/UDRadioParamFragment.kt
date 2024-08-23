package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RadioCommunicateEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdRadioParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDRadioParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/22
 * @desc: 电台配置
 *
 */
class UDRadioParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdRadioParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDRadioParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var radioChannelList: List<String> = emptyList()
    private val transmitPowerList: List<String> = (10..22).map { it.toString() }//发射功率
    private val airSpeedList: List<String> = (1..3).map { it.toString() }//空中速率

    override fun initViewModel() {
        super.initViewModel()
    }

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
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        //451.15-470.15，1MHz步进
        radioChannelList = (451150..470150 step 1000).map {
            (it.toFloat() / 1000).toString() + "MHz"
        }
        resetParams()
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

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            disableOrEnable(if (isChecked) "1" else "0")
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
            resetParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun resetParams() {
        //发送默认 10 即 461.125MHz
        mStates.sendChannel.set(radioChannelList[0])
        //接收默认 20 即 471.125MHz
        mStates.receiveChannel.set(radioChannelList[0])
        //发射功率 [10~22] 默认22
        mStates.transmitPower.set(transmitPowerList[transmitPowerList.lastIndex])
        //空中速率  [1~3] 默认1
        mStates.airSpeed.set(airSpeedList[0])
    }

    /**
     * 关闭或者打开
     */
    private fun disableOrEnable(sw: String = "1") {
        commandItems.clear()
//        val command = IOTCommandUtil.getCommand(
//            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH,
//            "sw=$sw"
//        )
//        commandItems.add(command)

//        showLoadingDialog(StringUtils.getString(R.string.processing))
//        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        val entity = RadioCommunicateEntity(
            rxchl = radioChannelList.indexOf(mStates.receiveChannel.get()).toString(),
            txchl = radioChannelList.indexOf(mStates.sendChannel.get()).toString(),
            outpwr = mStates.transmitPower.get(),
            airbaud = mStates.airSpeed.get(),
        )
        //devicetype  添加且赋值为1时，表示配置自组网网关
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
        //devicetype  添加且赋值为1时，表示配置自组网网关
        var command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_RADIO_CTRL, "devicetype=1"
        ) else
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_RADIO_CTRL
            )
        commandItems.add(command)

        command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_TERMINAL_ID, "type=1"
        ) else IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_TERMINAL_ID
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
            mStates.receiveChannel.set(
                if (info.rxchl.toInt() in radioChannelList.indices) {
                    radioChannelList[info.rxchl.toInt()]
                } else {
                    radioChannelList[6]
                }
            )
            mStates.sendChannel.set(
                if (info.txchl.toInt() in radioChannelList.indices) {
                    radioChannelList[info.txchl.toInt()]
                } else {
                    radioChannelList[13]
                }
            )
            mStates.transmitPower.set(info.outpwr)
            mStates.airSpeed.set(info.airbaud)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}