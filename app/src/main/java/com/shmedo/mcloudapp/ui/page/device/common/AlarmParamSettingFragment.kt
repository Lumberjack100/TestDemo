package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmMonitorPointEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmReportIntervalEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmReportIntervalInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAlarmParamSettingBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AlarmParamSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class AlarmParamSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAlarmParamSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AlarmParamSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val monitorPointList: List<String> = (1..10).map { it.toString() }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_alarm_param_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAlarmParamSettingBinding
        binding.llToolbar.toolbar.title = "报警参数设置"
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
        initTitles()
        resetDefaultParams()
    }

    private fun initTitles() {
        when (productType) {
            ProductType.GNSS_M_1,//M20S
            ProductType.GNSS_M_2,
            ProductType.U_R_1 //一体化雨量计
            -> {
                mStates.firstAlarmThresholdTitle.set("一级报警阈值(毫米)")
                mStates.secondAlarmThresholdTitle.set("二级报警阈值(毫米)")
                mStates.thirdAlarmThresholdTitle.set("三级报警阈值(毫米)")
                mStates.fourthAlarmThresholdTitle.set("四级报警阈值(毫米)")

                mStates.firstAlarmReportIntervalTitle.set("一级报警间隔(秒)")
                mStates.secondAlarmReportIntervalTitle.set("二级报警间隔(秒)")
                mStates.thirdAlarmReportIntervalTitle.set("三级报警间隔(秒)")
                mStates.fourthAlarmReportIntervalTitle.set("四级报警间隔(秒)")
            }

            ProductType.U_I_1 -> {//倾斜仪
                mStates.firstAlarmThresholdTitle.set("一级报警阈值(度)")
                mStates.secondAlarmThresholdTitle.set("二级报警阈值(度)")
                mStates.thirdAlarmThresholdTitle.set("三级报警阈值(度)")
                mStates.fourthAlarmThresholdTitle.set("四级报警阈值(度)")

                mStates.firstAlarmReportIntervalTitle.set("一级报警间隔(秒)")
                mStates.secondAlarmReportIntervalTitle.set("二级报警间隔(秒)")
                mStates.thirdAlarmReportIntervalTitle.set("三级报警间隔(秒)")
                mStates.fourthAlarmReportIntervalTitle.set("四级报警间隔(秒)")
            }

            else -> {}
        }
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

        //一级报警阈值 默认40
        mStates.firstAlarmThreshold.set("40")
        //二级报警阈值 默认20
        mStates.secondAlarmThreshold.set("20")
        //三级报警阈值 默认10
        mStates.thirdAlarmThreshold.set("10")
        //四级报警阈值 默认5
        mStates.fourthAlarmThreshold.set("5")

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

        if (mStates.firstAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入一级报警阈值!")
            return
        }
        try {
            val value = mStates.firstAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的一级报警阈值!")
            return
        }

        if (mStates.secondAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入二级报警阈值!")
            return
        }
        try {
            val value = mStates.secondAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的二级报警阈值!")
            return
        }

        if (mStates.thirdAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入三级报警阈值!")
            return
        }
        try {
            val value = mStates.thirdAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的三级报警阈值!")
            return
        }

        if (mStates.fourthAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入四级报警阈值!")
            return
        }
        try {
            val value = mStates.fourthAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的四级报警阈值!")
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

        commandItems.clear()

        val entity = AlarmMonitorPointEntity(
            monitorpoint = mStates.monitorPoint.get(),
            cnt = mStates.broadcastTimes.get(),
            level1 = mStates.firstAlarmVoice.get(),
            level2 = mStates.secondAlarmVoice.get(),
            level3 = mStates.thirdAlarmVoice.get(),
            level4 = mStates.fourthAlarmVoice.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
            entity.toCommandString()
        )
        commandItems.add(command)

        val triggerValueEntity =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) AlarmTriggerValueEntity(
                devlevel1 = mStates.firstAlarmThreshold.get(),
                devlevel2 = mStates.secondAlarmThreshold.get(),
                devlevel3 = mStates.thirdAlarmThreshold.get(),
                devlevel4 = mStates.fourthAlarmThreshold.get()
            )
            else
                AlarmTriggerValueEntity(
                    level1 = mStates.firstAlarmThreshold.get(),
                    level2 = mStates.secondAlarmThreshold.get(),
                    level3 = mStates.thirdAlarmThreshold.get(),
                    level4 = mStates.fourthAlarmThreshold.get()
                )
        val triggerValueCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE,
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
            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL,
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
            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询语音参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<AlarmTriggerValueInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警阈值出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL -> {
                val result = iotParseManager.parse<AlarmReportIntervalInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警间隔出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置语音参数出错: ${result.message}"
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

            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警阈值出错: ${result.message}"
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

            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警间隔出错: ${result.message}"
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


    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
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
     * 初始化报警阈值数据
     */
    private fun initAlarmTriggerValueData(info: AlarmTriggerValueInfo) {
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            mStates.firstAlarmThreshold.set(info.devlevel1.formatDoubleValue("", 3))
            mStates.secondAlarmThreshold.set(info.devlevel2.formatDoubleValue("", 3))
            mStates.thirdAlarmThreshold.set(info.devlevel3.formatDoubleValue("", 3))
            mStates.fourthAlarmThreshold.set(info.devlevel4.formatDoubleValue("", 3))
        } else {
            mStates.firstAlarmThreshold.set(info.level1.formatDoubleValue("", 3))
            mStates.secondAlarmThreshold.set(info.level2.formatDoubleValue("", 3))
            mStates.thirdAlarmThreshold.set(info.level3.formatDoubleValue("", 3))
            mStates.fourthAlarmThreshold.set(info.level4.formatDoubleValue("", 3))
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