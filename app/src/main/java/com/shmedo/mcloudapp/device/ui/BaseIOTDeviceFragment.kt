package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.PageRefreshLayout
import com.hjq.toast.Toaster
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.IdleResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.ReadyResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.ext.getLogItem
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.device.common.CmdResponseResultError
import com.shmedo.mcloudapp.device.common.CmdResponseResultSuccess
import com.shmedo.mcloudapp.device.common.CmdResponseResultTimeOut
import com.shmedo.mcloudapp.device.common.DispatchFailed
import com.shmedo.mcloudapp.device.common.DispatchSuccess
import com.shmedo.mcloudapp.device.common.NoDeviceState
import com.shmedo.mcloudapp.device.common.WorkingState
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.NetIOTCommandViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import java.util.LinkedList

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/20 <br/>
 * 描述：     TODO
 */
abstract class BaseIOTDeviceFragment : BaseFragment() {
    protected lateinit var mMessenger: PageMessenger
    protected lateinit var netIotCommandViewModel: NetIOTCommandViewModel
    protected lateinit var bleViewModel: BleViewModel
    protected lateinit var logViewModel: LogViewModel

    protected var refreshLayout: PageRefreshLayout? = null

    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null
    private var timeoutJob: Job? = null

    protected var commandItems = LinkedList<String>()

    @CallSuper
    override fun initViewModel() {
        mMessenger = getAppViewModel()
        netIotCommandViewModel = getViewModel()
        bleViewModel = getViewModel()
        logViewModel = getViewModel()
    }

    @CallSuper
    override fun initData() {
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
    }

    @CallSuper
    override fun createObserver() {
        launchWithViewLifecycle {
            collectNetData()
        }
        launchWithViewLifecycle {
            collectBleData()
        }
    }

    private suspend fun collectNetData() {
        netIotCommandViewModel.cmdDispatchFlow.collect {
            when (it) {
                is DispatchFailed -> {
                    addLogItem(LogLevel.ERROR, "DispatchFailed: ${it.errorMsg}")
                    doNetDispatchFailed(it.cmdStr, it.errorMsg)
                }

                is DispatchSuccess -> {
                    doNetDispatchSuccess(it.cmdStr)
                }

                is CmdResponseResultError -> {
                    addLogItem(LogLevel.ERROR, "Response Error: ${it.errorMsg}")
                    doCmdResponseResultError(it.cmdStr, it.errorMsg)
                }

                is CmdResponseResultTimeOut -> {
                    addLogItem(LogLevel.ERROR, "Response TimeOut")
                    doCmdResponseResultTimeOut(it.cmdStr, it.errorMsg)
                }

                is CmdResponseResultSuccess -> {
                    addLogItem(LogLevel.INFO, it.cmdResult.responseContent)
                    setResultData(it.cmdResult.responseContent)
                }

                else -> {}
            }
        }
    }

    private suspend fun collectBleData() {
        bleViewModel.state.collect { state ->
            Timber.v("${javaClass.simpleName} MedoBle: $state")
//                if (isRestrictHiddenMode() && isHidden) {
//                    return@collect
//                }
            when (state) {
                NoDeviceState -> {}
                is WorkingState -> when (state.result) {
                    is IdleResult,
                    is ConnectingResult -> {
                        addLogItem(LogLevel.INFO, "device ${bleDevice?.address} connecting")
                        showLoadingDialog(StringUtils.getString(R.string.ble_state_connecting))
                    }

                    is ConnectedResult -> {
                        addLogItem(LogLevel.INFO, "device ${bleDevice?.address} connected")
//                        dismissLoadingDialog()
//                        onConnectionStateChanged(true)
                    }

                    is ReadyResult -> {
                        addLogItem(LogLevel.INFO, "device ready")
                        onConnectionStateChanged(true)
                        onBleDeviceReady()
                    }

                    is SuccessResult -> {
                        addLogItem(LogLevel.INFO, state.result.data.response)
                        setResultData(state.result.data.response)
                    }

                    is DisconnectedResult -> {
                        addLogItem(
                            LogLevel.ERROR,
                            "device disconnected, reason: ${state.result.reason}"
                        )
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is LinkLossResult -> {
                        addLogItem(LogLevel.ERROR, "device link loss")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is MissingServiceResult -> {
                        addLogItem(LogLevel.ERROR, "device missing service")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is UnknownErrorResult -> {
                        addLogItem(LogLevel.ERROR, "device unknown error")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }
                }
            }
        }
    }

    open fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("指令发送失败: $errorMsg")
        dismissLoadingDialog()
        refreshLayout?.finish(false)
    }

    open fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult(cmdStr = cmdStr)
    }

    open fun doCmdResponseResultError(cmdStr: String, errorMsg: String) {
        Toaster.show("指令响应错误: $errorMsg")
        dismissLoadingDialog()
        refreshLayout?.finish(false)
    }

    open fun doCmdResponseResultTimeOut(cmdStr: String, errorMsg: String) {
        Toaster.show("指令响应超时")
        dismissLoadingDialog()
        refreshLayout?.finish(false)
    }

    open fun onConnectionStateChanged(isConnected: Boolean) {

    }

    open fun onBleDeviceReady() {
        dismissLoadingDialog()
    }

    abstract fun setResultData(cmdStr: String)

    /**
     * 发送调试指令
     */
    protected fun sendDebugCommand(
        command: String
    ) {
        addLogItem(LogLevel.INFO, command)
        bleViewModel.sendIOTCommand(command, false, deviceInfo.apikey)
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected inline fun sendCommandFromCmdList(
        delaySendMillis: Long = 0,//默认不延迟发送
        isStartTimeoutJob: Boolean = false,//默认不启动超时Job
        timeoutMillis: Long = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
        crossinline finishAction: () -> Unit = {}
    ) {
        if (commandItems.size <= 0) {
            cancelNearbyCommunicationTimeoutJob()
            finishAction()
            return
        }

        val command = commandItems.first
        commandItems.removeFirst()
        addLogItem(LogLevel.INFO, command)

        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendIOTCommand(command, true, deviceInfo.apikey, delaySendMillis)
            if (isStartTimeoutJob)
                startNearbyCommunicationTimeoutJob(command, timeoutMillis)
        }
    }

    /**
     * 启动蓝牙通讯/WIFI通信等近场通信超时 Job
     */
    fun startNearbyCommunicationTimeoutJob(
        cmdStr: String = "",
        timeMillis: Long = AppContants.Communication.DELAY_10000_MILLIS
    ) {
        // 启动一个新的协程作为超时Job
        timeoutJob = launchWithViewLifecycle {
            delay(timeMillis) // 延迟 timeMillis 秒后，提示超时
            withContext(Dispatchers.Main) {
                showNearbyCommunicationTimeoutAlert(cmdStr = cmdStr)
            }
        }
    }

    /**
     * 取消蓝牙通讯/WIFI通信等近场通信超时 Job
     */
    @CallSuper
    protected open fun cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog: Boolean = true) {
        timeoutJob?.cancel()
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
        commandItems.clear()
        refreshLayout?.finish(false)
    }

    /**
     * 显示近场通信超时提示
     */
    protected open fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String = "",
        isDismissLoadingDialog: Boolean = true,
        isShowMsg: Boolean = true,
        msg: String = ""
    ) {
        Timber.i("${javaClass.simpleName} 发送指令超时")
        addLogItem(LogLevel.ERROR, "发送指令超时")
        if (isShowMsg) {
            Toaster.show(msg.ifEmpty { "发送指令超时,请稍后尝试" })
        }
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
        timeoutJob?.cancel()
        commandItems.clear()
        refreshLayout?.finish(false)
    }

    fun addLogItem(priority: Int, data: String) {
        logViewModel.insertLog(
            getLogItem(
                sessionId = MmkvCacheUtil.getIOTDeviceLogSessionId(),
                priority = priority,
                data = data
            )
        )
    }

    /**
     *
     */
    override fun isRestrictHiddenMode(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutJob?.cancel() // 在Fragment销毁时取消timeoutJob
    }

    companion object {
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}