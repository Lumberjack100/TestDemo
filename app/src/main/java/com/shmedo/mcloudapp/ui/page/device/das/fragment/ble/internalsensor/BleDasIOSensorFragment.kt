package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.internalsensor

import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTRainStation
import com.shmedo.lib.cmd.base.md_cmd.enums.MDBreakAlarmStatus
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDRainStation
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.model.das.BreakAlarmStatusInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.DasBaseConfigInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseDasIOSensorClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasIoSensorBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasIOSensorViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasIOSensorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasIoSensorBinding
    private lateinit var mStates: DasIOSensorViewModel
    private val mdParseManager: MDParserManager by inject()
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
    private val rainResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.rain_value) }
    private var breakAlarmStatus: MDBreakAlarmStatus = MDBreakAlarmStatus.QUERY

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_io_sensor,
            BR.stateVM,
            mStates
        )
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
            queryIoStatus()
        }
    }

    override fun initData() {
        super.initData()
        mStates.switchType.set(IOTRainStation.CLOSE)
        mStates.rainResolution.set(rainResolutionList[0])
    }

    inner class ClickProxy : BaseDasIOSensorClickProxy() {
        override fun onModeCheckedChanged(view: View) {
            when (view.id) {
                R.id.radio_close -> {
                    mStates.switchType.set(IOTRainStation.CLOSE)
                    setIOSensorState(MDRainStation.CLOSE.toString())
                }

                R.id.radio_rain_gauge -> {
                    mStates.switchType.set(IOTRainStation.RAIN_OPEN)
                    setIOSensorState(MDRainStation.RAIN_OPEN.toString())
                }

                R.id.radio_break_alarm -> {
                    mStates.switchType.set(IOTRainStation.ALARM_OPEN)
                    setIOSensorState(MDRainStation.ALARM_OPEN.toString())
                }
            }
        }

        override fun onChooseResolutionClick() {
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .atView(binding.llRainResolution) // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(rainResolutionList, null, { _, text ->
                    mStates.rainResolution.set(text)
                }, 0, 0)
                .show()
        }

        override fun onBreakAlarmCheckedChanged(view: View) {
            when (view.id) {
                R.id.radio_break_alarm_open -> {
                    mStates.isBreakAlarmOpen.set(true)
                    queryOrSetBreakAlarmState(MDBreakAlarmStatus.OPEN.toString())
                }

                R.id.radio_break_alarm_close -> {
                    mStates.isBreakAlarmOpen.set(false)
                    queryOrSetBreakAlarmState(MDBreakAlarmStatus.CLOSE.toString())
                }
            }
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            setRainPrecision()
        }
    }

    /**
     * 设置开关量传感器启用情况
     */
    private fun setIOSensorState(value: String) {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.RAIN_STATION,
            value
        )
        commandItems.add(command)
        Timber.d("设置开关量指令==%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun queryOrSetBreakAlarmState(value: String) {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.BREAK_ALARM_STATUS,
            value
        )
        commandItems.add(command)
        Timber.d("查询/设置断线报警器指令==%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun setRainPrecision() {
        val rainResolution = (mStates.rainResolution.get().toDouble() * 100).toInt()

        commandItems.clear()
        var command = MDCommandUtil.getCommand(
            MDCommandType.SETTING_RAIN_PRECISION,
            rainResolution
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询开关量传感器状态
     */
    private fun queryIoStatus() {
        commandItems.clear()
        val command =
            MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
        Timber.d("获取基础配置信息指令===%s", command)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.BASE_CONFIG -> {
                val result = mdParseManager.parse<DasBaseConfigInfo>(
                    cmdStr,
                    MDCommandType.BASE_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询开关量传感器状态出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initIOStatus(result.data)
                    }
                }
            }

            MDCommandType.BREAK_ALARM_STATUS -> {
                val result =
                    if (breakAlarmStatus == MDBreakAlarmStatus.QUERY) mdParseManager.parse<DasBaseConfigInfo>(
                        cmdStr,
                        MDCommandType.BREAK_ALARM_STATUS
                    ) else mdParseManager.parse<String>(cmdStr)

                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg =
                            if (breakAlarmStatus == MDBreakAlarmStatus.QUERY) "查询断线报警器状态出错" else "设置断线报警器出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        if (breakAlarmStatus == MDBreakAlarmStatus.QUERY) {
                            (result.data as? BreakAlarmStatusInfo)?.let {
                                initBreakAlarmStatus(it)
                            }
                        }
                    }
                }
            }

            MDCommandType.RAIN_STATION -> {//开关量传感器   0051：雨量计开启  0052：关闭  0053：断线报警器开启
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("0051")) "启用雨量计出错" else if (cmdStr.contains("0052")) "关闭开关量传感器出错"
                            else "启用断线报警器出错"

                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList() {
                            if (cmdStr.contains("0053")) {
                                //查询断线报警器状态
                                breakAlarmStatus = MDBreakAlarmStatus.QUERY
                                queryOrSetBreakAlarmState(MDBreakAlarmStatus.QUERY.toString())
                            }
                        }
                    }
                }
            }

            MDCommandType.SETTING_RAIN_PRECISION -> {//设置雨量计精度 ##121
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "雨量计精度配置错误!"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存参数出错!"
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

    private fun initIOStatus(info: DasBaseConfigInfo) {
        try {
            decimalFormat.applyPattern("#.#")
            when (MDRainStation.value(info.rainStation)) {
                MDRainStation.CLOSE -> {//2：关闭
                    mStates.switchType.set(IOTRainStation.CLOSE)
                }

                MDRainStation.RAIN_OPEN -> {//1：雨量计
                    mStates.switchType.set(IOTRainStation.RAIN_OPEN)
                    mStates.isSupportDumpMInTime.set(false)//翻斗翻转最小间隔
                    mStates.rainResolution.set(info.rainAccuracy.toDoubleOrNull()?.let {
                        decimalFormat.format(it / 10000)
                    } ?: "")
                }

                MDRainStation.ALARM_OPEN -> {//3：断线报警器
                    mStates.switchType.set(IOTRainStation.ALARM_OPEN)
                    //查询断线报警器状态
                    breakAlarmStatus = MDBreakAlarmStatus.QUERY
                    queryOrSetBreakAlarmState(MDBreakAlarmStatus.QUERY.toString())
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBreakAlarmStatus(info: BreakAlarmStatusInfo) {
        mStates.isBreakAlarmOpen.set(MDBreakAlarmStatus.value(info.status) == MDBreakAlarmStatus.OPEN)
    }


    companion object {
        fun newInstance() = BleDasIOSensorFragment()
    }
}