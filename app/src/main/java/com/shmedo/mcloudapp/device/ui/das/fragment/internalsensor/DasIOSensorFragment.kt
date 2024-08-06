package com.shmedo.mcloudapp.device.ui.das.fragment.internalsensor

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
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
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentDasIoSensorBinding
import com.shmedo.mcloudapp.device.common.BaseDasIOSensorClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasIOSensorViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class DasIOSensorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasIoSensorBinding
    private lateinit var mStates: DasIOSensorViewModel
    private val iotParseManager: IOTParserManager by inject()
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
    private val rainResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.rain_value) }


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
            queryData()
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
                    initSaveCommand(IOTRainStation.CLOSE.toString())
                }

                R.id.radio_rain_gauge -> {
                    mStates.switchType.set(IOTRainStation.RAIN_OPEN)
                }

                R.id.radio_break_alarm -> {
                    mStates.switchType.set(IOTRainStation.ALARM_OPEN)
                    initSaveCommand(IOTRainStation.ALARM_OPEN.toString(), value = IOTBreakAlarmStatus.OPEN.toString())
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
                    initSaveCommand(
                        IOTRainStation.ALARM_OPEN.toString(),
                        value = IOTBreakAlarmStatus.OPEN.toString()
                    )
                }

                R.id.radio_break_alarm_close -> {
                    mStates.isBreakAlarmOpen.set(false)
                    initSaveCommand(IOTRainStation.ALARM_OPEN.toString(), value = IOTBreakAlarmStatus.CLOSE.toString())
                }
            }
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand(
                type = mStates.switchType.get().toString(),
                value = mStates.rainResolution.get(),
                minTime = if (mStates.isSupportDumpMInTime.get() && !TextUtils.isEmpty(mStates.dumpMinTime.get())) mStates.dumpMinTime.get() else IOTConstants.NULL_KEY
            )
        }
    }

    private fun initSaveCommand(
        type: String,
        value: String = IOTConstants.NULL_KEY,
        minTime: String = IOTConstants.NULL_KEY
    ) {
        val entity = DasIOSensorEntity(
            type = type,
            value = value,
            min_time = minTime
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
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

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO -> {//查询开关量传感器参数
                val result = iotParseManager.parse<DasIOSensorInfo>(
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
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO -> {//设置开关量传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置开关量传感器参数出错: ${result.message}"
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

    private fun initParamData(ioSensorInfo: DasIOSensorInfo) {
        try {
            when (IOTRainStation.value(ioSensorInfo.type)) {
                IOTRainStation.CLOSE -> {//0：关闭开关量功能
                    mStates.switchType.set(IOTRainStation.CLOSE)
                }

                IOTRainStation.RAIN_OPEN -> {//1：雨量站模式
                    mStates.switchType.set(IOTRainStation.RAIN_OPEN)
                    ioSensorInfo.value.toDoubleOrNull()?.let { value ->
                        mStates.rainResolution.set(decimalFormat.format(value))
                    }
                }

                IOTRainStation.ALARM_OPEN -> {//2：断线报警器模式
                    mStates.switchType.set(IOTRainStation.ALARM_OPEN)
                    if (IOTBreakAlarmStatus.value(ioSensorInfo.value) == IOTBreakAlarmStatus.OPEN)
                        mStates.isBreakAlarmOpen.set(true)
                    else
                        mStates.isBreakAlarmOpen.set(false)
                }
            }
            if (ioSensorInfo.min_time != IOTConstants.NULL_KEY) {
                mStates.isSupportDumpMInTime.set(true)
                mStates.dumpMinTime.set(ioSensorInfo.min_time)
            } else
                mStates.isSupportDumpMInTime.set(false)

        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }


    companion object {
        fun newInstance() = DasIOSensorFragment()
    }
}