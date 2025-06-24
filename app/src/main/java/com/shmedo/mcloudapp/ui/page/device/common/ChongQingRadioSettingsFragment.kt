package com.shmedo.mcloudapp.ui.page.device.common

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
import com.shmedo.core.commonlib.utils.AppContants
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
import com.shmedo.mcloudapp.databinding.FragmentChongqingRadioSettingsBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ChongQingRadioSettingsViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class ChongQingRadioSettingsFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentChongqingRadioSettingsBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: ChongQingRadioSettingsViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var radioChannelList: List<String> = emptyList()
    private val transmitPowerList: List<String> = (0..22).map { it.toString() }//发射功率
    private val airSpeedList: List<String> = arrayListOf("0", "1", "2")//空中速率


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_chongqing_radio_settings,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentChongqingRadioSettingsBinding
        binding.llToolbar.toolbar.title = "电台配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
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
        radioChannelList = if (productType == ProductType.LB20S)
            (451125..480125 step 1000).map {
                (it.toFloat() / 1000).toString() + "MHz"
            }
        else
            (45015..46915 step 100).map {
                (it.toFloat() / 100).toString() + "MHz"
            }
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        when (productType) {
            ProductType.COLLECTOR_G_0 -> {//自组网网关 载波频率以 450.15Mhz 为起始，间隔 1Mhz，进行信道划分，共划分 20 个信道
                //发送默认 13 即 463.15MHz
                mStates.sendChannel.set(radioChannelList[13])
                //接收默认 6 即 456.15MHz
                mStates.receiveChannel.set(radioChannelList[6])
            }

            ProductType.LB20S -> {//LB20S 载波频率以 451.125 为起始，间隔 1Mhz，进行信道划分，共划分 30 个信道
                //发送默认 10 即 461.125MHz
                mStates.sendChannel.set(radioChannelList[10])
                //接收默认 20 即 471.125MHz
                mStates.receiveChannel.set(radioChannelList[20])
            }

            else -> {

            }
        }
        //发射功率 [0~22] 默认22
        mStates.transmitPower.set(transmitPowerList[transmitPowerList.lastIndex])
        //空中速率  [0~2] 默认1
        mStates.airSpeed.set(airSpeedList[1])

        mStates.telemetryStationNode1.set("")
        mStates.telemetryStationNode2.set("")
        mStates.telemetryStationNode3.set("")
        mStates.telemetryStationNode4.set("")
        mStates.telemetryStationNode5.set("")
        mStates.telemetryStationNode6.set("")
        mStates.telemetryStationNode7.set("")
        mStates.telemetryStationNode8.set("")
        mStates.telemetryStationNode9.set("")
        mStates.telemetryStationNode10.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
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

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
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

    private fun initSaveCommand() {
        if (mStates.telemetryStationNode1.get().isEmpty()) {
            showMessageDialog("请输入测站 1 编号!")
            return
        }
        if (mStates.telemetryStationNode2.get().isEmpty()) {
            showMessageDialog("请输入测站 2 编号!")
            return
        }
        if (mStates.telemetryStationNode3.get().isEmpty()) {
            showMessageDialog("请输入测站 3 编号!")
            return
        }
        if (mStates.telemetryStationNode4.get().isEmpty()) {
            showMessageDialog("请输入测站 4 编号!")
            return
        }
        if (mStates.telemetryStationNode5.get().isEmpty()) {
            showMessageDialog("请输入测站 5 编号!")
            return
        }
        if (mStates.telemetryStationNode6.get().isEmpty()) {
            showMessageDialog("请输入测站 6 编号!")
            return
        }
        if (mStates.telemetryStationNode7.get().isEmpty()) {
            showMessageDialog("请输入测站 7 编号!")
            return
        }
        if (mStates.telemetryStationNode8.get().isEmpty()) {
            showMessageDialog("请输入测站 8 编号!")
            return
        }
        if (mStates.telemetryStationNode9.get().isEmpty()) {
            showMessageDialog("请输入测站 9 编号!")
            return
        }
        if (mStates.telemetryStationNode10.get().isEmpty()) {
            showMessageDialog("请输入测站 10 编号!")
            return
        }
        commandItems.clear()
        val entity = RadioCommunicateEntity(
            rxchl = radioChannelList.indexOf(mStates.receiveChannel.get()).toString(),
            txchl = radioChannelList.indexOf(mStates.sendChannel.get()).toString(),
            outpwr = mStates.transmitPower.get(),
            airbaud = mStates.airSpeed.get(),
        )
        //devicetype  添加且赋值为1时，表示配置自组网网关
        var command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            "${entity.toCommandString()}&devicetype=1"
        ) else IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_RADIO_CTRL,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_DEL_TERMINAL_ID,
            "type=0&devicetype=1"
        ) else
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_DEL_TERMINAL_ID,
                "type=0"
            )
        commandItems.add(command)

        command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_TERMINAL_ID,
            "id=${mStates.telemetryStationNode1.get()},${mStates.telemetryStationNode2.get()},${mStates.telemetryStationNode3.get()},${mStates.telemetryStationNode4.get()},${mStates.telemetryStationNode5.get()},${mStates.telemetryStationNode6.get()},${mStates.telemetryStationNode7.get()},${mStates.telemetryStationNode8.get()},${mStates.telemetryStationNode9.get()},${mStates.telemetryStationNode10.get()}&devicetype=1"
        ) else
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_TERMINAL_ID,
                "id=${mStates.telemetryStationNode1.get()},${mStates.telemetryStationNode2.get()},${mStates.telemetryStationNode3.get()},${mStates.telemetryStationNode4.get()},${mStates.telemetryStationNode5.get()},${mStates.telemetryStationNode6.get()},${mStates.telemetryStationNode7.get()},${mStates.telemetryStationNode8.get()},${mStates.telemetryStationNode9.get()},${mStates.telemetryStationNode10.get()}"
            )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_25000_MILLIS
        )
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

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MD_GET_RADIO_CTRL)
                || (commandType == IOTCommandType.MD_GET_TERMINAL_ID)
                || (commandType == IOTCommandType.MD_SET_RADIO_CTRL)
                || (commandType == IOTCommandType.MD_DEL_TERMINAL_ID)
                || (commandType == IOTCommandType.MD_SET_TERMINAL_ID)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
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

            IOTCommandType.MD_GET_TERMINAL_ID -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_TERMINAL_ID
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询测站节点信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initTerminalIds(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_RADIO_CTRL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置电台参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            sendCommandFromCmdList {
                                Toaster.show("数据保存成功")
                            }
                        }, AppContants.Communication.DELAY_10000_MILLIS)
                    }
                }
            }

            IOTCommandType.MD_DEL_TERMINAL_ID -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "删除终端 ID 出错: ${result.message}"
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

            IOTCommandType.MD_SET_TERMINAL_ID -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置终端 ID 出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList { processNavigateUp() }
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

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun initTerminalIds(content: String) {
        if (content.isEmpty())
            return

        try {
            //用逗号分割
            val terminalIds = content.split(",".toRegex()).dropLastWhile { it.isEmpty() }

            mStates.telemetryStationNode1.set(
                if (terminalIds.isNotEmpty()) {
                    terminalIds[0]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode2.set(
                if (terminalIds.size > 1) {
                    terminalIds[1]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode3.set(
                if (terminalIds.size > 2) {
                    terminalIds[2]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode4.set(
                if (terminalIds.size > 3) {
                    terminalIds[3]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode5.set(
                if (terminalIds.size > 4) {
                    terminalIds[4]
                } else {
                    ""
                }
            )

            mStates.telemetryStationNode6.set(
                if (terminalIds.size > 5) {
                    terminalIds[5]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode7.set(
                if (terminalIds.size > 6) {
                    terminalIds[6]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode8.set(
                if (terminalIds.size > 7) {
                    terminalIds[7]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode9.set(
                if (terminalIds.size > 8) {
                    terminalIds[8]
                } else {
                    ""
                }
            )
            mStates.telemetryStationNode10.set(
                if (terminalIds.size > 9) {
                    terminalIds[9]
                } else {
                    ""
                }
            )

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
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