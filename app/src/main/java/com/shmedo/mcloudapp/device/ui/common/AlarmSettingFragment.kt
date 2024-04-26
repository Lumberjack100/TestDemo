package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.AlarmMonitorPointEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.AlarmReportIntervalEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.AlarmReportIntervalInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAlarmSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AlarmSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/4/25
 * @desc: 报警设置
 *
 */
class AlarmSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAlarmSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AlarmSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private var productType = ProductType.UnKnown
    private val monitorPointList: List<String> = (1..15).map { it.toString() }
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_alarm_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAlarmSettingBinding
        binding.llToolbar.toolbar.title = "报警设置"
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
        arguments?.let {
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
        }
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
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                disableAlram()
            }
        }

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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(1)
        }

        /**
         * 测试二级报警
         */
        fun onTestSecondAlarmClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(2)
        }

        /**
         * 测试三级报警
         */
        fun onTestThirdAlarmClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(3)
        }

        /**
         * 测试四级报警
         */
        fun onTestFourthAlarmClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(4)
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

    /**
     * 关闭水位计
     */
    private fun disableAlram() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_CTRL,
            "sw=0"
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initTestAlarmCommand(level: Int) {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_TEST_ALRAM,
            "level=$level"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun resetParams() {
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

        //一级报警位移阈值 默认40
        mStates.firstAlarmDisplacementThreshold.set("40")
        //二级报警位移阈值 默认20
        mStates.secondAlarmDisplacementThreshold.set("20")
        //三级报警位移阈值 默认10
        mStates.thirdAlarmDisplacementThreshold.set("10")
        //四级报警位移阈值 默认5
        mStates.fourthAlarmDisplacementThreshold.set("5")

        //一级报警上报间隔 默认60,单位s
        mStates.firstAlarmReportInterval.set("60")
        //二级报警上报间隔 默认300,单位s
        mStates.secondAlarmReportInterval.set("300")
        //三级报警上报间隔 默认1800,单位s
        mStates.thirdAlarmReportInterval.set("1800")
        //四级报警上报间隔 默认3600,单位s
        mStates.fourthAlarmReportInterval.set("3600")

    }

    private fun initSaveCommand() {
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

        if (mStates.firstAlarmDisplacementThreshold.get().isEmpty()) {
            showMessageDialog("请输入一级报警位移阈值!")
            return
        }
        try {
            val value = mStates.firstAlarmDisplacementThreshold.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的一级报警位移阈值!")
            return
        }

        if (mStates.secondAlarmDisplacementThreshold.get().isEmpty()) {
            showMessageDialog("请输入二级报警位移阈值!")
            return
        }
        try {
            val value = mStates.secondAlarmDisplacementThreshold.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的二级报警位移阈值!")
            return
        }

        if (mStates.thirdAlarmDisplacementThreshold.get().isEmpty()) {
            showMessageDialog("请输入三级报警位移阈值!")
            return
        }
        try {
            val value = mStates.thirdAlarmDisplacementThreshold.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的三级报警位移阈值!")
            return
        }

        if (mStates.fourthAlarmDisplacementThreshold.get().isEmpty()) {
            showMessageDialog("请输入四级报警位移阈值!")
            return
        }
        try {
            val value = mStates.fourthAlarmDisplacementThreshold.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的四级报警位移阈值!")
            return
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

        val entity = AlarmMonitorPointEntity(
            sw = if (mStates.isOpened.get()) "1" else "0",
            monitorpoint = mStates.monitorPoint.get(),
            cnt = mStates.broadcastTimes.get(),
            level1 = mStates.firstAlarmVoice.get(),
            level2 = mStates.secondAlarmVoice.get(),
            level3 = mStates.thirdAlarmVoice.get(),
            level4 = mStates.fourthAlarmVoice.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_CTRL,
            entity.toCommandString()
        )
        commandItems.add(command)

        val triggerValueEntity = AlarmTriggerValueEntity(
            devlevel1 = mStates.firstAlarmDisplacementThreshold.get(),
            devlevel2 = mStates.secondAlarmDisplacementThreshold.get(),
            devlevel3 = mStates.thirdAlarmDisplacementThreshold.get(),
            devlevel4 = mStates.fourthAlarmDisplacementThreshold.get()
        )
        val triggerValueCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_TRIGGER_VALUE,
            triggerValueEntity.toCommandString()
        )
        commandItems.add(triggerValueCommand)

        val reportIntervalEntity = AlarmReportIntervalEntity(
            level1 = mStates.firstAlarmReportInterval.get(),
            level2 = mStates.secondAlarmReportInterval.get(),
            level3 = mStates.thirdAlarmReportInterval.get(),
            level4 = mStates.fourthAlarmReportInterval.get()
        )
        val reportIntervalCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_REPORT_INTERVAL,
            reportIntervalEntity.toCommandString()
        )
        commandItems.add(reportIntervalCommand)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_CTRL
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_TRIGGER_VALUE
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_REPORT_INTERVAL
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_ALRAM_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_CTRL
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
                        initAlarmMonitorPointData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<AlarmTriggerValueInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_TRIGGER_VALUE
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
                        initAlarmTriggerValueData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_REPORT_INTERVAL -> {
                val result = iotParseManager.parse<AlarmReportIntervalInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_REPORT_INTERVAL
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
                        initAlarmReportIntervalData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_CTRL -> {//
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

            IOTCommandType.MD_SET_ALRAM_TRIGGER_VALUE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置报警阈值出错: ${result.message}"
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

            IOTCommandType.MD_SET_ALRAM_REPORT_INTERVAL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置报警间隔出错: ${result.message}"
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

            IOTCommandType.MD_TEST_ALRAM -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "发送预警测试指令出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("预警测试成功")
                        }
                    }
                }
            }


            else -> {}
        }
    }

    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
            mStates.isOpened.set(info.sw == "1")
            mStates.monitorPoint.set(info.monitorpoint)
            mStates.broadcastTimes.set(info.cnt)
            mStates.firstAlarmVoice.set(info.level1)
            mStates.secondAlarmVoice.set(info.level2)
            mStates.thirdAlarmVoice.set(info.level3)
            mStates.fourthAlarmVoice.set(info.level4)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * 初始化报警位移阈值数据
     */
    private fun initAlarmTriggerValueData(info: AlarmTriggerValueInfo) {
        decimalFormat.applyPattern("#.###")
        info.devlevel1.toDoubleOrNull()?.let {
            mStates.firstAlarmDisplacementThreshold.set(decimalFormat.format(it))
        }
        info.devlevel2.toDoubleOrNull()?.let {
            mStates.secondAlarmDisplacementThreshold.set(decimalFormat.format(it))
        }
        info.devlevel3.toDoubleOrNull()?.let {
            mStates.thirdAlarmDisplacementThreshold.set(decimalFormat.format(it))
        }
        info.devlevel4.toDoubleOrNull()?.let {
            mStates.fourthAlarmDisplacementThreshold.set(decimalFormat.format(it))
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
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}