package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceTimeInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentTimeCalibrationBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.TimeCalibrationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/10/18
 * @desc: 时间校准
 *
 */
class TimeCalibrationFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentTimeCalibrationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: TimeCalibrationViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private var isDoSetTimeCmd = false


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_time_calibration,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentTimeCalibrationBinding
        binding.llToolbar.toolbar.title = "时间校准"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
        super.initData()
        mStates.deviceTime.set("2024-10-18 10:00:00")
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (isBleDas()) {
                isDoSetTimeCmd = true
                initDasBleSaveCommand()
            }
            else{
                initSaveCommand()
            }
        }
    }

    private fun initDasBleSaveCommand() {
        val command =
            MDCommandUtil.getCommand(
                MDCommandType.LOCAL_TIME, TimeUtils.getNowString(
                    TimeUtils.getSafeDateFormat("yyMMddHHmmss")
                )
            )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    private fun initSaveCommand() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.SET_TERMINAL_TIME,
            "time=${TimeUtils.getNowString()}"
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        if (isBleDas()) {
            isDoSetTimeCmd = false
            queryDasBleTerminalTime()
        } else
            queryTerminalTime()
    }

    /**
     * 查询终端时间
     */
    private fun queryTerminalTime() {
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.loading),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * DAS 设备蓝牙模式下查询终端时间
     */
    private fun queryDasBleTerminalTime() {

        val command =
            MDCommandUtil.getCommand(MDCommandType.LOCAL_TIME)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.loading),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        if (isBleDas()) {
            handleDasBleCommandResult(cmdStr)
        } else {
            handleCommandResult(cmdStr)
        }
    }

    private fun handleDasBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.LOCAL_TIME -> {
                val result = mdParseManager.parse<DeviceTimeInfo>(
                    cmdStr,
                    MDCommandType.LOCAL_TIME
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询设备时间出错：${result.message}"
                        handleFailureResult(errMsg)
                    }

                    is MDCommandResult.Success -> {
                        if (!isDoSetTimeCmd) {
                            initDeviceTime(result.data.time)
                        }else{
                            if (!isCommunicationExecuting())
                            processNavigateUp("校准成功")
                        }
                    }
                }
            }

            else -> {
            }
        }
    }

    private fun handleCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备时间出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initDeviceTime(result.data)
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {//设置终端时间
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "校准出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp("校准成功")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initDeviceTime(deviceTime: String) {
        mStates.deviceTime.set(deviceTime)
        mStates.systemTime.set(TimeUtils.getNowString())
        mStates.timeDifference.set(
            TimeUtils.getFitTimeSpan(
                TimeUtils.getNowString(),
                deviceTime,
                4
            )
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}