package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasIOSensorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTBreakAlarmStatus
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTRainStation
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasIOSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDBreakAlarmStatus
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDRainStation
import com.shmedo.lib.cmd.base.md_cmd.model.das.BreakAlarmStatusInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.DasBaseConfigInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasIoSensorBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasIOSensorViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/17
 * @desc: 物联网采集器(DAS)开关量传感器参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasIOSensorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasIoSensorBinding
    private val mStates: DasIOSensorViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private val modeList = arrayListOf("关闭", "雨量计", "断线报警器") // 模式
    private val rainResolutionList by lazy {
        Utils.getApp().resources.getStringArray(R.array.rain_value)
    }
    private val breakAlarmModeList = arrayListOf("常开", "常闭")

    private var breakAlarmStatus: MDBreakAlarmStatus = MDBreakAlarmStatus.QUERY


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_io_sensor, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasIoSensorBinding
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
        resetDefaultParams()
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.mode.set("关闭")
        mStates.rainResolution.set(rainResolutionList[0])
        mStates.dumpMinTime.set("")
        mStates.breakAlarmMode.set(breakAlarmModeList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        /** 模式选择 */
        fun onModeChooseClick() {
            val selectedIndex = modeList.indexOf(mStates.mode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "",
                    modeList.toTypedArray(),
                    null,
                    selectedIndex,
                    { position, text -> mStates.mode.set(text) },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onChooseResolutionClick() {
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .atView(binding.tvRainResolution) // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(
                    rainResolutionList,
                    null,
                    { _, text -> mStates.rainResolution.set(text) },
                    0,
                    0
                )
                .show()
        }

        /**
         */
        fun onBreakAlarmModeChooseClick() {
            val selectedIndex = breakAlarmModeList.indexOf(mStates.breakAlarmMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "",
                    breakAlarmModeList.toTypedArray(),
                    null,
                    selectedIndex,
                    { position, text -> mStates.breakAlarmMode.set(text) },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 恢复默认配置 */
        override fun onResetButtonClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

            if (communicateWay == BleConnect) {
                initBleSaveCommand()
            } else {
                init4GSaveCommand()
            }
        }
    }

    /** 蓝牙通讯模式保存指令 */
    private fun initBleSaveCommand() {
        commandItems.clear()

        when (modeList.indexOf(mStates.mode.get())) {
            1 -> {
                var command = MDCommandUtil.getCommand(
                    MDCommandType.RAIN_STATION,
                    MDRainStation.RAIN_OPEN.toString()
                )
                commandItems.add(command)
                Timber.d("设置开关量指令==%s", command)

                val rainResolution = (mStates.rainResolution.get().toDouble() * 100).toInt()
                command = MDCommandUtil.getCommand(
                    MDCommandType.SETTING_RAIN_PRECISION,
                    rainResolution
                )
                commandItems.add(command)
            }

            2 -> {
                var command = MDCommandUtil.getCommand(
                    MDCommandType.RAIN_STATION,
                    MDRainStation.ALARM_OPEN.toString()
                )
                commandItems.add(command)
                Timber.d("设置开关量指令==%s", command)

                command = MDCommandUtil.getCommand(
                    MDCommandType.BREAK_ALARM_STATUS,
                    breakAlarmModeList.indexOf(mStates.breakAlarmMode.get()) + 1
                )
                commandItems.add(command)
                Timber.d("查询/设置断线报警器指令==%s", command)
                breakAlarmStatus = MDBreakAlarmStatus.OPEN
            }

            else -> {
                var command = MDCommandUtil.getCommand(
                    MDCommandType.RAIN_STATION,
                    MDRainStation.CLOSE.toString()
                )
                commandItems.add(command)
                Timber.d("设置开关量指令==%s", command)
            }
        }

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /** 4G通讯模式保存指令 */
    private fun init4GSaveCommand() {
        val entity =
            DasIOSensorEntity(
                type = modeList.indexOf(mStates.mode.get()).toString(),
                value = when (modeList.indexOf(mStates.mode.get())) {
                    1 -> mStates.rainResolution.get()
                    2 -> breakAlarmModeList.indexOf(mStates.breakAlarmMode.get()).toString()
                    else -> IOTConstants.NULL_KEY
                },
                min_time = if (mStates.supportDumpMInTime.get() &&
                    !TextUtils.isEmpty(mStates.dumpMinTime.get())
                )
                    mStates.dumpMinTime.get()
                else IOTConstants.NULL_KEY
            )
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(
                IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO,
                entity.toCommandString()
            )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /** 查询数据 */
    private fun queryData() {
        commandItems.clear()
        if (communicateWay == BleConnect) {
            // 蓝牙模式：查询开关量传感器状态
            var command = MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
            commandItems.add(command)
            Timber.d("获取基础配置信息指令===%s", command)

            command =
                MDCommandUtil.getCommand(
                    MDCommandType.BREAK_ALARM_STATUS,
                    MDBreakAlarmStatus.QUERY.toString()
                )
            commandItems.add(command)
            breakAlarmStatus = MDBreakAlarmStatus.QUERY

        } else {
            // 4G模式：查询开关量传感器参数
            val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO)
            commandItems.add(command)
        }
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = true,
            isMessageDialog = true
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
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = true,
            isMessageDialog = true
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
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = true,
            isMessageDialog = true,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        // 判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        if (communicateWay == BleConnect) {
            handleBleResult(cmdStr)
        } else {
            handle4GResult(cmdStr)
        }
    }

    /** 处理4G模式的结果 */
    private fun handle4GResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO -> { // 查询开关量传感器参数
                val result =
                    iotParseManager.parse<DasIOSensorInfo>(
                        cmdStr,
                        IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询开关量传感器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { binding.refreshLayout.finish() }
                        init4GIOStatus(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO -> { // 设置开关量传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /** 4G模式：初始化参数数据 */
    private fun init4GIOStatus(ioSensorInfo: DasIOSensorInfo) {
        try {
            when (IOTRainStation.value(ioSensorInfo.type)) {
                IOTRainStation.CLOSE -> { // 0：关闭开关量功能
                    mStates.mode.set(modeList[0])
                }

                IOTRainStation.RAIN_OPEN -> { // 1：雨量站模式
                    mStates.mode.set(modeList[1])
                    mStates.rainResolution.set(ioSensorInfo.value.formatDoubleValue("0.1", 1))
                    if (ioSensorInfo.min_time != IOTConstants.NULL_KEY) {
                        mStates.supportDumpMInTime.set(true)
                        mStates.dumpMinTime.set(ioSensorInfo.min_time)
                    } else mStates.supportDumpMInTime.set(false)
                }

                IOTRainStation.ALARM_OPEN -> { // 2：断线报警器模式
                    mStates.mode.set(modeList[2])
                    if (IOTBreakAlarmStatus.value(ioSensorInfo.value) == IOTBreakAlarmStatus.OPEN)
                        mStates.breakAlarmMode.set(breakAlarmModeList[0])
                    else mStates.breakAlarmMode.set(breakAlarmModeList[1])
                }
            }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /** 处理蓝牙模式的结果 */
    private fun handleBleResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.BASE_CONFIG -> {
                val result =
                    mdParseManager.parse<DasBaseConfigInfo>(cmdStr, MDCommandType.BASE_CONFIG)
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询传感器状态出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList { binding.refreshLayout.finish() }
                        initBleIOStatus(result.data)
                    }
                }
            }

            MDCommandType.BREAK_ALARM_STATUS -> {
                val result =
                    if (breakAlarmStatus == MDBreakAlarmStatus.QUERY)
                        mdParseManager.parse<DasBaseConfigInfo>(
                            cmdStr,
                            MDCommandType.BREAK_ALARM_STATUS
                        )
                    else mdParseManager.parse<String>(cmdStr)

                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg =
                            if (breakAlarmStatus == MDBreakAlarmStatus.QUERY) "查询断线报警器状态出错"
                            else "断线报警器配置出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            if (breakAlarmStatus == MDBreakAlarmStatus.QUERY)
                                binding.refreshLayout.finish()
                            else {
                                Toaster.show("数据保存成功")
                                mStates.saveInitialState()
                            }
                        }
                        if (breakAlarmStatus == MDBreakAlarmStatus.QUERY) {
                            (result.data as? BreakAlarmStatusInfo)?.let { initBreakAlarmStatus(it) }
                        }
                    }
                }
            }

            MDCommandType.RAIN_STATION -> { //开关量传感器   0051：雨量计开启  0052：关闭  0053：断线报警器开启
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("0051")) "雨量计配置出错"
                            else if (cmdStr.contains("0052")) "关闭传感器出错" else "断线报警器配置出错"

                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList() {
                            Toaster.show("数据保存成功")
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SETTING_RAIN_PRECISION -> { // 设置雨量计精度 ##121
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "雨量计配置出错!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList() {
                            Toaster.show("数据保存成功")
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /** 蓝牙模式：初始化IO状态 */
    private fun initBleIOStatus(info: DasBaseConfigInfo) {
        try {
            when (MDRainStation.value(info.rainStation)) {
                MDRainStation.CLOSE -> { // 2：关闭
                    mStates.mode.set(modeList[0])
                }

                MDRainStation.RAIN_OPEN -> { // 1：雨量计
                    mStates.mode.set(modeList[1])
                    mStates.supportDumpMInTime.set(false) // 翻斗翻转最小间隔
                    mStates.rainResolution.set(
                        info.rainAccuracy.toDoubleOrNull()?.let {
                            (it / 10000).toString().formatDoubleValue("0.1", 1)
                        }
                            ?: "0.1"
                    )
                }

                MDRainStation.ALARM_OPEN -> { // 3：断线报警器
                    mStates.mode.set(modeList[2])
                }
            }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /** 蓝牙模式：初始化断线报警状态 */
    private fun initBreakAlarmStatus(info: BreakAlarmStatusInfo) {
        if (MDBreakAlarmStatus.value(info.status) == MDBreakAlarmStatus.OPEN)
            mStates.breakAlarmMode.set(breakAlarmModeList[0])
        else mStates.breakAlarmMode.set(breakAlarmModeList[1])

        mStates.saveInitialState()
    }

    companion object {
        fun newInstance() = DasIOSensorFragment()
    }
}
