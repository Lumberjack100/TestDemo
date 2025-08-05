package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRAlarmModuleParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port3SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRAlarmModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3AcousticOpticalAlarmParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port3AcousticOpticalAlarmParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/** 创建者: gonghe <br/> 创建时间: 2023/10/16 <br/> 描述： RS485-3接口声光报警器参数配置 */
class MR702RS485Port3AcousticOpticalAlarmParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port3AcousticOpticalAlarmParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702RS485Port3AcousticOpticalAlarmParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy {
        Utils.getApp().resources.getStringArray(R.array.mr_check_bit)
    }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port3_acoustic_optical_alarm_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port3AcousticOpticalAlarmParamBinding
        binding.llToolbar.toolbar.title = "声光报警器"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? -> processBack(true) }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
        initRefresh()
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

    override fun initData() {
        super.initData()
        initDefaultParam()
    }

    /** 初始化默认参数 */
    private fun initDefaultParam() {
        mStates.status.set("已接入")
        mStates.isOpened.set(true)
        mStates.baudRate.set("9600")
        mStates.dataBit.set(dataBitList[3])
        mStates.checkBit.set(checkBitList[0])
        mStates.stopBit.set(stopBitList[0])

        mStates.waterLevelFunctionEnabled.set(true)
        mStates.waterLevelRelayK.set(true)
        mStates.waterLevelTriggerValue1.set("80")
        mStates.waterLevelTriggerValue2.set("60")
        mStates.waterLevelTriggerValue3.set("40")
        mStates.waterLevelTriggerValue4.set("20")
        mStates.waterLevelHoldTime1.set("")
        mStates.waterLevelHoldTime2.set("")
        mStates.waterLevelHoldTime3.set("")
        mStates.waterLevelHoldTime4.set("")
        mStates.waterLevelGapTime1.set("")
        mStates.waterLevelGapTime2.set("")
        mStates.waterLevelGapTime3.set("")
        mStates.waterLevelGapTime4.set("")
        mStates.waterLevelVoiceIndex1.set("84")
        mStates.waterLevelVoiceIndex2.set("83")
        mStates.waterLevelVoiceIndex3.set("82")
        mStates.waterLevelVoiceIndex4.set("81")
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                showMessage(
                    "确定要关闭吗？",
                    "温馨提示",
                    "确定",
                    { closeSwitch() },
                    "取消",
                    {
                        mStates.isOpened.set(true)
                        (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                    }
                )
            }
        }

        fun onDataBitChooseClick() {
            val selectedIndex = dataBitList.indexOf(mStates.dataBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据位",
                    dataBitList,
                    null,
                    selectedIndex,
                    { _, text -> mStates.dataBit.set(text) },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCheckBitChooseClick() {
            val selectedIndex = checkBitList.indexOf(mStates.checkBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择校验位",
                    checkBitList,
                    null,
                    selectedIndex,
                    { _, text -> mStates.checkBit.set(text) },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onStopBitChooseClick() {
            val selectedIndex = stopBitList.indexOf(mStates.stopBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择停止位",
                    stopBitList,
                    null,
                    selectedIndex,
                    { _, text -> mStates.stopBit.set(text) },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
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

    private fun closeSwitch() {
        commandItems.clear()
        val entity =
            MRRS485Port3SensorParamEntity(
                device = "2", // 声光报警器的设备类型是2
                switch = "0"
            )
        val command =
            IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM,
                entity.toCommandString()
            )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        // 检查水位参数有效性
        if (!checkWaterLevelParameters()) {
            return
        }

        val entity =
            MRRS485Port3SensorParamEntity(
                device = "2", // 声光报警器的设备类型是2
                switch = "1",
                addr = mStates.address.get(),
                baud = mStates.baudRate.get(),
                databit = mStates.dataBit.get(),
                paritybit = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
                stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString()
            )
        val command =
            IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM,
                entity.toCommandString()
            )
        commandItems.add(command)

        val entity2 = MRAlarmModuleParamEntity(
            index = "1",
            switch = if (mStates.waterLevelFunctionEnabled.get()) "1" else "0",
            relay = if (mStates.waterLevelRelayK.get()) "1" else "0",
            warnlevel = mStates.getWaterLevelWarnLevelString(),
            holdtime1 = mStates.waterLevelHoldTime1.get(),
            holdtime2 = mStates.waterLevelHoldTime2.get(),
            holdtime3 = mStates.waterLevelHoldTime3.get(),
            holdtime4 = mStates.waterLevelHoldTime4.get(),
            gaptime1 = mStates.waterLevelGapTime1.get(),
            gaptime2 = mStates.waterLevelGapTime2.get(),
            gaptime3 = mStates.waterLevelGapTime3.get(),
            gaptime4 = mStates.waterLevelGapTime4.get(),
            voiceindex1 = mStates.generateVoiceCommand("38", mStates.waterLevelVoiceIndex1.get()),
            voiceindex2 = mStates.generateVoiceCommand("38", mStates.waterLevelVoiceIndex2.get()),
            voiceindex3 = mStates.generateVoiceCommand("38", mStates.waterLevelVoiceIndex3.get()),
            voiceindex4 = mStates.generateVoiceCommand("38", mStates.waterLevelVoiceIndex4.get()),
            respindex1 = mStates.defaultRespCommand,
            respindex2 = mStates.defaultRespCommand,
            respindex3 = mStates.defaultRespCommand,
            respindex4 = mStates.defaultRespCommand,
            respindex5 = mStates.defaultRespCommand
        )
        // 构建水位报警参数指令
        val waterLevelCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_ALARM_MODULE,
            entity2.toCommandString()
        )
        commandItems.add(waterLevelCommand)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun checkWaterLevelParameters(): Boolean {
        if (mStates.waterLevelTriggerValue1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警阈值")
            return false
        }

        if (mStates.waterLevelTriggerValue2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警阈值")
            return false
        }

        if (mStates.waterLevelTriggerValue3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警阈值")
            return false
        }

        if (mStates.waterLevelTriggerValue4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警阈值")
            return false
        }

        // 检查水位报警持续时间
        if (mStates.waterLevelHoldTime1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警持续时间")
            return false
        }

        if (mStates.waterLevelHoldTime2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警持续时间")
            return false
        }

        if (mStates.waterLevelHoldTime3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警持续时间")
            return false
        }

        if (mStates.waterLevelHoldTime4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警持续时间")
            return false
        }

        // 检查水位报警间隔时间
        if (mStates.waterLevelGapTime1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警间隔时间")
            return false
        }

        if (mStates.waterLevelGapTime2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警间隔时间")
            return false
        }

        if (mStates.waterLevelGapTime3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警间隔时间")
            return false
        }

        if (mStates.waterLevelGapTime4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警间隔时间")
            return false
        }

        if (mStates.waterLevelVoiceIndex1.get().isEmpty()) {
            showMessageDialog("请输入水位一级报警语音编号")
            return false
        }
        try {
            val value = mStates.waterLevelVoiceIndex1.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位一级报警语音编号范围[1,255]!")
                return false
            }

            // 生成等级1语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand("255", mStates.waterLevelVoiceIndex1.get())
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位一级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex2.get().isEmpty()) {
            showMessageDialog("请输入水位二级报警语音编号")
            return false
        }
        try {
            val value = mStates.waterLevelVoiceIndex2.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位二级报警语音编号范围[1,255]!")
                return false
            }

            // 生成等级2语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand("255", mStates.waterLevelVoiceIndex2.get())
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位二级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex3.get().isEmpty()) {
            showMessageDialog("请输入水位三级报警语音编号")
            return false
        }
        try {
            val value = mStates.waterLevelVoiceIndex3.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位三级报警语音编号范围[1,255]!")
                return false
            }

            // 生成等级3语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand("255", mStates.waterLevelVoiceIndex3.get())
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位三级报警语音编号!")
            return false
        }

        if (mStates.waterLevelVoiceIndex4.get().isEmpty()) {
            showMessageDialog("请输入水位四级报警语音编号")
            return false
        }
        try {
            val value = mStates.waterLevelVoiceIndex4.get().toInt()
            if (value < 1 || value > 255) {
                showMessageDialog("水位四级报警语音编号范围[1,255]!")
                return false
            }

            // 生成等级4语音报警发送指令，检查 CRC-16 校验是否正确
            mStates.generateVoiceCommand("255", mStates.waterLevelVoiceIndex4.get())
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
        var command =
            IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM,
                "device=2" // 声光报警器的设备类型是2
            )
        commandItems.add(command)

        // 查询水位报警参数
        val waterLevelCommand =
            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_ALARM_MODULE, "index=1")
        commandItems.add(waterLevelCommand)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM)
                || (commandType == IOTCommandType.MR_MD_GET_ALARM_MODULE)
                || (commandType == IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM)
                || (commandType == IOTCommandType.MR_MD_SET_ALARM_MODULE)

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
            IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM -> {
                val result =
                    iotParseManager.parse<MRRS485Port3SensorParam>(
                        cmdStr,
                        IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { binding.refreshLayout.finish() }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_ALARM_MODULE -> {
                val result =
                    iotParseManager.parse<MRAlarmModuleParam>(
                        cmdStr,
                        IOTCommandType.MR_MD_GET_ALARM_MODULE
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { binding.refreshLayout.finish() }
                        parseAlarmModuleData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            processBack()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_SET_ALARM_MODULE -> {
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

    private fun initParamData(sensorParam: MRRS485Port3SensorParam) {
        try {
            mStates.status.set(if (sensorParam.switch == "1") "已接入" else "未接入")
            mStates.isOpened.set(sensorParam.switch == "1")
            mStates.address.set(sensorParam.addr)
            mStates.baudRate.set(sensorParam.baud)
            mStates.dataBit.set(sensorParam.databit)
            sensorParam.paritybit.toInt().let {
                if (it in checkBitList.indices) {
                    mStates.checkBit.set(checkBitList[it])
                }
            }
            sensorParam.stopbit.toInt().let {
                if (it in stopBitList.indices) {
                    mStates.stopBit.set(stopBitList[it])
                }
            }
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun parseAlarmModuleData(alarmModuleParam: MRAlarmModuleParam) {
        launchWithViewLifecycle {
            try {
                // 水位报警参数
                mStates.waterLevelFunctionEnabled.set(alarmModuleParam.switch == "1")
                mStates.waterLevelRelayK.set(alarmModuleParam.relay == "1")

                // 解析报警阈值
                mStates.setWaterLevelWarnLevels(alarmModuleParam.warnlevel)

                // 设置各级报警持续时间
                mStates.waterLevelHoldTime1.set(alarmModuleParam.holdtime1)
                mStates.waterLevelHoldTime2.set(alarmModuleParam.holdtime2)
                mStates.waterLevelHoldTime3.set(alarmModuleParam.holdtime3)
                mStates.waterLevelHoldTime4.set(alarmModuleParam.holdtime4)

                // 设置各级报警间隔时间
                mStates.waterLevelGapTime1.set(alarmModuleParam.gaptime1)
                mStates.waterLevelGapTime2.set(alarmModuleParam.gaptime2)
                mStates.waterLevelGapTime3.set(alarmModuleParam.gaptime3)
                mStates.waterLevelGapTime4.set(alarmModuleParam.gaptime4)

                mStates.waterLevelVoiceIndex1.set(
                    mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex1)
                )
                mStates.waterLevelVoiceIndex2.set(
                    mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex2)
                )
                mStates.waterLevelVoiceIndex3.set(
                    mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex3)
                )
                mStates.waterLevelVoiceIndex4.set(
                    mStates.getVoiceIndexNumber(alarmModuleParam.voiceindex4)
                )
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(
                    if (statusBarColor == 0) R.color.colorPrimary else statusBarColor
                )
                nav().navigateUp()
                return@launchWithViewLifecycle
            }
            delay(1000)
            // 需要给上一级浏览页面传递最新的事件信息
            setFragmentResult(
                MR702RS485Port3Fragment.FRAGMENT_RESULT_REQUEST_KEY,
                bundleOf(MR702RS485Port3Fragment.REFRESH_DATA to true)
            )
            nav().navigateUp()
        }
    }

    companion object {
        fun newBundleArguments(
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle =
            Bundle().apply {
                putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
                putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
                putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
                putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
                putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
            }
    }
}