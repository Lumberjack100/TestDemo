package com.shmedo.mcloudapp.device.ui.das.fragment.internalsensor

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasIOSensorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.das.DasIOSensorInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentDasIoSensorBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasIOSensorViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class DasIOSensorFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentDasIoSensorBinding by lazy { getBinding() as FragmentDasIoSensorBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DasIOSensorViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
    private val rainResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.rain_value) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_io_sensor,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "开关量传感器"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
        initRefresh()
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

    override fun initData() {
        super.initData()
        mStates.checkMode.set(0)
        mStates.rainResolution.set(rainResolutionList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onModeCheckedChanged(view: View) {
            when (view.id) {
                R.id.radio_close -> {
                    mStates.checkMode.set(0)
                    initSaveCommand("0")
                }

                R.id.radio_rain_gauge -> {
                    mStates.checkMode.set(1)
                }

                R.id.radio_break_alarm -> {
                    mStates.checkMode.set(2)
                    initSaveCommand("2", "0")
                }
            }
        }

        fun onChooseResolutionClick() {
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

        fun onBreakAlarmCheckedChanged(view: View) {
            when (view.id) {
                R.id.radio_break_alarm_open -> {
                    mStates.isBreakAlarmOpen.set(true)
                    initSaveCommand("2", "0")
                }

                R.id.radio_break_alarm_close -> {
                    mStates.isBreakAlarmOpen.set(false)
                    initSaveCommand("2", "1")
                }
            }
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand(
                type = mStates.checkMode.get().toString(),
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询开关量传感器参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置开关量传感器参数出错: ${result.message}"
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

            else -> {}
        }
    }

    private fun initParamData(ioSensorInfo: DasIOSensorInfo) {
        try {
            ioSensorInfo.type.let {
                when (it) {
                    "1" -> {
                        mStates.checkMode.set(1)
                        ioSensorInfo.value.toDoubleOrNull()?.let { value ->
                            mStates.rainResolution.set(decimalFormat.format(value))
                        }
                    }

                    "2" -> {
                        mStates.checkMode.set(2)
                        if (ioSensorInfo.value == "0")
                            mStates.isBreakAlarmOpen.set(true)
                        else
                            mStates.isBreakAlarmOpen.set(false)
                    }

                    else -> {
                        mStates.checkMode.set(0)
                    }
                }
            }
            if (ioSensorInfo.min_time != IOTConstants.NULL_KEY) {
                mStates.isSupportDumpMInTime.set(true)
                mStates.dumpMinTime.set(ioSensorInfo.min_time)
            } else
                mStates.isSupportDumpMInTime.set(false)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}