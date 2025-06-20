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
import com.shmedo.mcloudapp.databinding.FragmentTimeCalibrationBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.TimeCalibrationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/10/18
 * @desc: 时间校准
 *
 */
class TimeCalibrationFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentTimeCalibrationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: TimeCalibrationViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private var isDoSetTimeCmd = false

    override fun initViewModel() {
        super.initViewModel()
    }

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
            if (isBleDisconnected()) {
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
        commandItems.clear()
        val command =
            MDCommandUtil.getCommand(
                MDCommandType.LOCAL_TIME, TimeUtils.getNowString(
                    TimeUtils.getSafeDateFormat("yyMMddHHmmss")
                )
            )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.SET_TERMINAL_TIME,
            "time=${TimeUtils.getNowString()}"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
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
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * DAS 设备蓝牙模式下查询终端时间
     */
    private fun queryDasBleTerminalTime() {
        commandItems.clear()

        val command =
            MDCommandUtil.getCommand(MDCommandType.LOCAL_TIME)
        commandItems.add(command)

        Timber.d("获取设备时间信息指令===%s", command)
        showLoadingDialog(StringUtils.getString(R.string.loading))
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
            errMsg = "出错了：$errMsg",
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
            errMsg = "设备未响应",
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
            errMsg = "设备未响应"
        )
    }

    override fun setResultData(cmdStr: String) {
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
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList { }
                        if (!isDoSetTimeCmd) {
                            initDeviceTime(result.data.time)
                        }else{
                            processNavigateUp("校准成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
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
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {}
                        initDeviceTime(result.data)
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {//设置终端时间
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "校准出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp("校准成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
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