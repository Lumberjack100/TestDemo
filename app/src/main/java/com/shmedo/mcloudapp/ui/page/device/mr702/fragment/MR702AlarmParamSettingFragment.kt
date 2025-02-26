package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRAlarmModuleParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRAlarmModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702AlarmParamSettingBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702AlarmParamSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702AlarmParamSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702AlarmParamSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702AlarmParamSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_alarm_param_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702AlarmParamSettingBinding
        binding.llToolbar.toolbar.title = "报警参数设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
        initRefresh()
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
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
        resetDefaultParam()
    }

    private fun resetDefaultParam() {
        mStates.rainfallFunctionEnabled.set(true)
        mStates.rainfallRelayK.set(true)
        mStates.rainfallRS4853.set(true)
        mStates.rainfallHoldTime.set("")
        mStates.rainfallGapTime.set("")
        mStates.rainfallClearGapTime.set("")
        mStates.rainfallBroadcastTimes.set("3")
        mStates.rainfallWarnLevel1.set("80")
        mStates.rainfallWarnLevel2.set("60")
        mStates.rainfallWarnLevel3.set("40")
        mStates.rainfallWarnLevel4.set("20")
        mStates.rainfallVoiceIndex1.set("84")
        mStates.rainfallVoiceIndex2.set("83")
        mStates.rainfallVoiceIndex3.set("82")
        mStates.rainfallVoiceIndex4.set("81")

        mStates.waterLevelFunctionEnabled.set(true)
        mStates.waterLevelRelayK.set(true)
        mStates.waterLevelRS4853.set(true)
        mStates.waterLevelHoldTime.set("")
        mStates.waterLevelGapTime.set("")
        mStates.waterLevelClearGapTime.set("")
        mStates.waterfallBroadcastTimes.set("3")
        mStates.waterLevelWarnLevel1.set("80")
        mStates.waterLevelWarnLevel2.set("60")
        mStates.waterLevelWarnLevel3.set("40")
        mStates.waterLevelWarnLevel4.set("20")
        mStates.waterLevelVoiceIndex1.set("84")
        mStates.waterLevelVoiceIndex2.set("83")
        mStates.waterLevelVoiceIndex3.set("82")
        mStates.waterLevelVoiceIndex4.set("81")
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        override fun onSubmitButtonClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            saveAlarmModuleParam()
        }
    }

    private fun saveAlarmModuleParam() {
        // 检查参数有效性
        if (!checkRainfallParameters() || !checkWaterLevelParameters()) {
            return
        }
        commandItems.clear()
        var entity = MRAlarmModuleParamEntity(
            index = "0",
            switch = if (mStates.rainfallFunctionEnabled.get()) "1" else "0",
            relay = if (mStates.rainfallRelayK.get()) "1" else "0",
            holdtime = mStates.rainfallHoldTime.get(),
            gaptime = mStates.rainfallGapTime.get(),
            cleargaptime = mStates.rainfallClearGapTime.get(),
            warnlevel = mStates.getRainfallWarnLevelString(),
            voiceindex1 = mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex1.get()
            ),
            voiceindex2 = mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex2.get()
            ),
            voiceindex3 = mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex3.get()
            ),
            voiceindex4 = mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex4.get()
            ),
            respindex1 = mStates.defaultRespCommand,
            respindex2 = mStates.defaultRespCommand,
            respindex3 = mStates.defaultRespCommand,
            respindex4 = mStates.defaultRespCommand,
            respindex5 = mStates.defaultRespCommand
        )
        // 构建雨量报警参数指令
        val rainfallCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_ALARM_MODULE,
            entity.toCommandString()
        )
        commandItems.add(rainfallCommand)

        entity = MRAlarmModuleParamEntity(
            index = "1",
            switch = if (mStates.waterLevelFunctionEnabled.get()) "1" else "0",
            relay = if (mStates.waterLevelRelayK.get()) "1" else "0",
            holdtime = mStates.waterLevelHoldTime.get(),
            gaptime = mStates.waterLevelGapTime.get(),
            cleargaptime = mStates.waterLevelClearGapTime.get(),
            warnlevel = mStates.getWaterLevelWarnLevelString(),
            voiceindex1 = mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex1.get()
            ),
            voiceindex2 = mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex2.get()
            ),
            voiceindex3 = mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex3.get()
            ),
            voiceindex4 = mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex4.get()
            ),
            respindex1 = mStates.defaultRespCommand,
            respindex2 = mStates.defaultRespCommand,
            respindex3 = mStates.defaultRespCommand,
            respindex4 = mStates.defaultRespCommand,
            respindex5 = mStates.defaultRespCommand
        )
        // 构建水位报警参数指令
        val waterLevelCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_ALARM_MODULE,
            entity.toCommandString()
        )
        commandItems.add(waterLevelCommand)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun checkRainfallParameters(): Boolean {
        if (mStates.rainfallHoldTime.get().isEmpty()) {
            showMessageDialog("请输入雨量报警持续时间")
            return false
        }
        if (mStates.rainfallGapTime.get().isEmpty()) {
            showMessageDialog("请输入雨量报警间隔时间")
            return false
        }
        if (mStates.rainfallClearGapTime.get().isEmpty()) {
            showMessageDialog("请输入雨量消警间隔时间")
            return false
        }
        if(mStates.rainfallBroadcastTimes.get().isEmpty()) {
            showMessageDialog("请输入雨量广播次数")
            return false
        }
        try {
            val value = mStates.rainfallBroadcastTimes.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("雨量广播次数范围[1,255]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的雨量广播次数!")
            return false
        }

        if (mStates.rainfallWarnLevel1.get().isEmpty()) {
            showMessageDialog("请输入雨量一级报警阈值")
            return false
        }
        if (mStates.rainfallWarnLevel2.get().isEmpty()) {
            showMessageDialog("请输入雨量二级报警阈值")
            return false
        }
        if (mStates.rainfallWarnLevel3.get().isEmpty()) {
            showMessageDialog("请输入雨量三级报警阈值")
            return false
        }
        if (mStates.rainfallWarnLevel4.get().isEmpty()) {
            showMessageDialog("请输入雨量四级报警阈值")
            return false
        }
        if (mStates.rainfallVoiceIndex1.get().isEmpty()) {
            showMessageDialog("请输入雨量一级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex1.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("雨量一级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级1语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex1.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的雨量一级报警语音编号!")
            return false
        }

        if (mStates.rainfallVoiceIndex2.get().isEmpty()) {
            showMessageDialog("请输入雨量二级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex2.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("雨量二级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级2语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex2.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的雨量二级报警语音编号!")
            return false
        }

        if (mStates.rainfallVoiceIndex3.get().isEmpty()) {
            showMessageDialog("请输入雨量三级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex3.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("雨量三级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级3语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex3.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的雨量三级报警语音编号!")
            return false
        }

        if (mStates.rainfallVoiceIndex4.get().isEmpty()) {
            showMessageDialog("请输入雨量四级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex4.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("雨量四级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级4语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.rainfallBroadcastTimes.get(),
                mStates.rainfallVoiceIndex4.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的雨量四级报警语音编号!")
            return false
        }

        return true
    }

    private fun checkWaterLevelParameters(): Boolean {
        if (mStates.waterLevelHoldTime.get().isEmpty()) {
            showMessageDialog("请输入水位报警持续时间")
            return false
        }
        if (mStates.waterLevelGapTime.get().isEmpty()) {
            showMessageDialog("请输入水位报警间隔时间")
            return false
        }
        if (mStates.waterLevelClearGapTime.get().isEmpty()) {
            showMessageDialog("请输入水位消警间隔时间")
            return false
        }
        if(mStates.waterfallBroadcastTimes.get().isEmpty()) {
            showMessageDialog("请输入水位广播次数")
            return false
        }
        try {
            val value = mStates.waterfallBroadcastTimes.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位广播次范围[1,255]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位广播次数!")
            return false
        }

        if (mStates.waterLevelWarnLevel1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警阈值")
            return false
        }
        if (mStates.waterLevelWarnLevel2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警阈值")
            return false
        }
        if (mStates.waterLevelWarnLevel3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警阈值")
            return false
        }
        if (mStates.waterLevelWarnLevel4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警阈值")
            return false
        }
        if (mStates.waterLevelVoiceIndex1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex1.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位一级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级1语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex1.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位一级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex2.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位二级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级2语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex2.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位二级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex3.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位三级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级3语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex3.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位三级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警语音编号")
            return false
        }
        try {
            val value = mStates.rainfallVoiceIndex4.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位四级报警语音编号范围[1,255]!")
                return false
            }

            //生成等级4语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand(
                mStates.waterfallBroadcastTimes.get(),
                mStates.waterLevelVoiceIndex4.get()
            )
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位四级报警语音编号!")
            return false
        }
        return true
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        // 查询雨量报警参数
        val rainfallCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_ALARM_MODULE,
            "index=0"
        )
        commandItems.add(rainfallCommand)

        // 查询水位报警参数
        val waterLevelCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_ALARM_MODULE,
            "index=1"
        )
        commandItems.add(waterLevelCommand)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_ALARM_MODULE -> {
                val result = iotParseManager.parse<MRAlarmModuleParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_ALARM_MODULE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        parseAlarmModuleData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_ALARM_MODULE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "报警参数保存出错：${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(ToastParams().apply {
                                text = "数据保存成功"
                                duration = 500
                            })
                            processBack()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun parseAlarmModuleData(alarmModuleParam: MRAlarmModuleParam) {
        launchWithViewLifecycle {
            try {
                // 提取index值，判断是雨量还是水位报警参数
                if (alarmModuleParam.index == "0") {
                    // 雨量报警参数
                    mStates.rainfallFunctionEnabled.set(alarmModuleParam.switch == "1")
                    mStates.rainfallRelayK.set(alarmModuleParam.relay == "1")
                    mStates.rainfallHoldTime.set(alarmModuleParam.holdtime)
                    mStates.rainfallGapTime.set(alarmModuleParam.gaptime)
                    mStates.rainfallClearGapTime.set(alarmModuleParam.cleargaptime)
                    mStates.rainfallBroadcastTimes.set(mStates.getBroadcastTimes(alarmModuleParam.voiceindex1))
                    mStates.setRainfallWarnLevels(alarmModuleParam.warnlevel)

                    mStates.rainfallVoiceIndex1.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex1))
                    mStates.rainfallVoiceIndex2.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex2))
                    mStates.rainfallVoiceIndex3.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex3))
                    mStates.rainfallVoiceIndex4.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex4))
                } else {
                    // 水位报警参数
                    mStates.waterLevelFunctionEnabled.set(alarmModuleParam.switch == "1")
                    mStates.waterLevelRelayK.set(alarmModuleParam.relay == "1")
                    mStates.waterLevelHoldTime.set(alarmModuleParam.holdtime)
                    mStates.waterLevelGapTime.set(alarmModuleParam.gaptime)
                    mStates.waterLevelClearGapTime.set(alarmModuleParam.cleargaptime)
                    mStates.waterfallBroadcastTimes.set(mStates.getBroadcastTimes(alarmModuleParam.voiceindex1))
                    mStates.setWaterLevelWarnLevels(alarmModuleParam.warnlevel)

                    mStates.waterLevelVoiceIndex1.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex1))
                    mStates.waterLevelVoiceIndex2.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex2))
                    mStates.waterLevelVoiceIndex3.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex3))
                    mStates.waterLevelVoiceIndex4.set(mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex4))
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }
            delay(1000)
            nav().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}