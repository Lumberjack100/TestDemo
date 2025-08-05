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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmMonitorPointEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmReportIntervalEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmReportIntervalInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdAlarmParamSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDAlarmParamSettingViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/23
 * @desc: 一体式雷达水位/泥位计报警参数设置
 *
 */
class UDAlarmParamSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdAlarmParamSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDAlarmParamSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val monitorPointList: List<String> = (1..10).map { it.toString() }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_alarm_param_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdAlarmParamSettingBinding
        binding.llToolbar.toolbar.title = "报警参数配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        initTitles()
        resetDefaultParams()
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    private fun initTitles() {
        mStates.firstAlarmReportIntervalTitle.set("一级报警间隔（秒）")
        mStates.secondAlarmReportIntervalTitle.set("二级报警间隔（秒）")
        mStates.thirdAlarmReportIntervalTitle.set("三级报警间隔（秒）")
        mStates.fourthAlarmReportIntervalTitle.set("四级报警间隔（秒）")
    }

    private fun resetDefaultParams() {
        //监测点编号 [1~15] 默认01
        mStates.monitorPoint.set("1")
        //播报次数 [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
        mStates.broadcastTimes.set("3")
        //一级报警语音编号  [1~255] 默认 4
        mStates.firstAlarmVoice.set("4")
        //二级报警语音编号  [1~255] 默认 3
        mStates.secondAlarmVoice.set("3")
        //三级报警语音编号  [1~255] 默认 2
        mStates.thirdAlarmVoice.set("2")
        //四级报警语音编号  [1~255] 默认 1

        //一级报警上报间隔 默认60,单位s
        mStates.firstAlarmReportInterval.set("60")
        //二级报警上报间隔 默认300,单位s
        mStates.secondAlarmReportInterval.set("300")
        //三级报警上报间隔 默认1800,单位s
        mStates.thirdAlarmReportInterval.set("1800")
        //四级报警上报间隔 默认3600,单位s
        mStates.fourthAlarmReportInterval.set("3600")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择报警编号
         */
        fun onMonitorPointChooseClick() {
            val selectedIndex = monitorPointList.indexOf(mStates.monitorPoint.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", monitorPointList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.monitorPoint.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 测试一级报警
         */
        fun onTestFirstAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(1)
        }

        /**
         * 测试二级报警
         */
        fun onTestSecondAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(2)
        }

        /**
         * 测试三级报警
         */
        fun onTestThirdAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(3)
        }

        /**
         * 测试四级报警
         */
        fun onTestFourthAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(4)
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
            initSaveCommand()
        }
    }

    private fun initTestAlarmCommand(level: Int) {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_TEST_ALRAM_BROADCAST,
            "level=$level"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        if (mStates.isOpened.get()) {
            if (mStates.broadcastTimes.get().isEmpty()) {
                showMessageDialog("请输入播报次数!")
                return
            }
            try {
                val value = mStates.broadcastTimes.get().toInt()
                if (value < 0 || value > 255) {
                    showMessageDialog("播报次数范围[0,255]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的播报次数!")
                return
            }

            if (mStates.firstAlarmVoice.get().isEmpty()) {
                showMessageDialog("请输入一级报警语音编号!")
                return
            }
            try {
                val value = mStates.firstAlarmVoice.get().toInt()
                if (value < 1 || value > 255) {
                    showMessageDialog("一级报警语音编号范围[1,255]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的一级报警语音编号!")
                return
            }

            if (mStates.secondAlarmVoice.get().isEmpty()) {
                showMessageDialog("请输入二级报警语音编号!")
                return
            }
            try {
                val value = mStates.secondAlarmVoice.get().toInt()
                if (value < 1 || value > 255) {
                    showMessageDialog("二级报警语音编号范围[1,255]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的二级报警语音编号!")
                return
            }

            if (mStates.thirdAlarmVoice.get().isEmpty()) {
                showMessageDialog("请输入三级报警语音编号!")
                return
            }
            try {
                val value = mStates.thirdAlarmVoice.get().toInt()
                if (value < 1 || value > 255) {
                    showMessageDialog("三级报警语音编号范围[1,255]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的三级报警语音编号!")
                return
            }

            if (mStates.fourthAlarmVoice.get().isEmpty()) {
                showMessageDialog("请输入四级报警语音编号!")
                return
            }
            try {
                val value = mStates.fourthAlarmVoice.get().toInt()
                if (value < 1 || value > 255) {
                    showMessageDialog("四级报警语音编号范围[1,255]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的四级报警语音编号!")
                return
            }
        }

        if (mStates.firstAlarmReportInterval.get().isEmpty()) {
            showMessageDialog("请输入一级报警上报间隔!")
            return
        }
        try {
            val value = mStates.firstAlarmReportInterval.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的一级报警上报间隔!")
            return
        }

        if (mStates.secondAlarmReportInterval.get().isEmpty()) {
            showMessageDialog("请输入二级报警上报间隔!")
            return
        }
        try {
            val value = mStates.secondAlarmReportInterval.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的二级报警上报间隔!")
            return
        }

        if (mStates.thirdAlarmReportInterval.get().isEmpty()) {
            showMessageDialog("请输入三级报警上报间隔!")
            return
        }
        try {
            val value = mStates.thirdAlarmReportInterval.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的三级报警上报间隔!")
            return
        }

        if (mStates.fourthAlarmReportInterval.get().isEmpty()) {
            showMessageDialog("请输入四级报警上报间隔!")
            return
        }
        try {
            val value = mStates.fourthAlarmReportInterval.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的四级报警上报间隔!")
            return
        }

        commandItems.clear()
        //UD 设备报警启用开关打开或者关闭，都需要发送开关指令
        if (productType == ProductType.U_D_1 || productType == ProductType.U_D_2 || productType == ProductType.U_D_3) {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH,
                "sw=${if (mStates.isOpened.get()) "1" else "0"}"
            )
            commandItems.add(command)
        }

        //GNSS 设备报警启用开关关闭时处理
        if (!mStates.isOpened.get() && (productType == ProductType.GNSS_M_5)) {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
                "sw=0"
            )
            commandItems.add(command)
        }

        //报警启用开关打开时，才发送报警信息设置指令
        if (mStates.isOpened.get()) {
            val monitorPointEntity = AlarmMonitorPointEntity(
                sw = if (productType == ProductType.GNSS_M_5)
                    "1"
                else IOTConstants.NULL_KEY,
                monitorpoint = mStates.monitorPoint.get(),
                cnt = mStates.broadcastTimes.get(),
                level1 = mStates.firstAlarmVoice.get(),
                level2 = mStates.secondAlarmVoice.get(),
                level3 = mStates.thirdAlarmVoice.get(),
                level4 = mStates.fourthAlarmVoice.get()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
                monitorPointEntity.toCommandString()
            )
            commandItems.add(command)
        }

        val reportIntervalEntity = AlarmReportIntervalEntity(
            level1 = mStates.firstAlarmReportInterval.get(),
            level2 = mStates.secondAlarmReportInterval.get(),
            level3 = mStates.thirdAlarmReportInterval.get(),
            level4 = mStates.fourthAlarmReportInterval.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL,
            reportIntervalEntity.toCommandString()
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

        if (productType == ProductType.U_D_1 || productType == ProductType.U_D_2 || productType == ProductType.U_D_3) {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
            )
            commandItems.add(command)
        }

        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH)
                || (commandType == IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL)
                || (commandType == IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL)
                || (commandType == IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH)
                || (commandType == IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL)
                || (commandType == IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL)
                || (commandType == IOTCommandType.MD_TEST_ALRAM_BROADCAST)

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
            IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH -> {
                val result = iotParseManager.parse<AlarmSwitchInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAlarmSwitch(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询语音参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAlarmMonitorPointData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL -> {
                val result = iotParseManager.parse<AlarmReportIntervalInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警间隔出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAlarmReportIntervalData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("sw=0")) "关闭出错: ${result.message}" else "打开出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("sw=0")) "关闭出错: ${result.message}" else "设置报警信息出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警间隔出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_TEST_ALRAM_BROADCAST -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "发送预警测试指令出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showMessageDialog("预警测试成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initAlarmSwitch(info: AlarmSwitchInfo) {
        try {
            mStates.isOpened.set(info.sw == "1")
            //添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
            info.sw.notNullKey {
                mStates.isOpened.set(it == "1")
            }
            mStates.monitorPoint.set(info.monitorpoint)
            mStates.broadcastTimes.set(info.cnt)
            mStates.firstAlarmVoice.set(info.level1)
            mStates.secondAlarmVoice.set(info.level2)
            mStates.thirdAlarmVoice.set(info.level3)
            mStates.fourthAlarmVoice.set(info.level4)

            //添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * 初始化报警上报间隔数据
     */
    private fun initAlarmReportIntervalData(info: AlarmReportIntervalInfo) {
        mStates.firstAlarmReportInterval.set(info.level1)
        mStates.secondAlarmReportInterval.set(info.level2)
        mStates.thirdAlarmReportInterval.set(info.level3)
        mStates.fourthAlarmReportInterval.set(info.level4)

        //添加这行来保存初始状态
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