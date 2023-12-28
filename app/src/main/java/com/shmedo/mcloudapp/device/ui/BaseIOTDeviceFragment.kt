package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
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
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
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
import timber.log.Timber
import java.util.LinkedList

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/20 <br/>
 * 描述：     TODO
 */
abstract class BaseIOTDeviceFragment : BaseFragment() {
    private val fragmentName by lazy { javaClass.simpleName }
    protected val mMessenger: PageMessenger by lazy { getAppViewModel() }
    protected val netIotCommandViewModel: NetIOTCommandViewModel by viewModels()
    protected val bleViewModel: BleViewModel by viewModels()
    protected val logViewModel: LogViewModel by activityViewModels()

    protected var refreshLayout: PageRefreshLayout? = null

    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null
    private var timeoutJob: Job? = null

    protected var commandItems = LinkedList<String>()


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
        collectNetData()
        collectBleData()
    }

    private fun collectNetData() {
        launchAndRepeatWithViewLifecycle {
            netIotCommandViewModel.cmdDispatchFlow.collect {
                when (it) {
                    is DispatchFailed -> {
                        addLogItem(priority = Log.ERROR, data = "DispatchFailed: ${it.errorMsg}")
                        doNetDispatchFailed(it.cmdStr, it.errorMsg)
                    }

                    is DispatchSuccess -> {
                        doNetDispatchSuccess(it.cmdStr)
                    }

                    is CmdResponseResultError -> {
                        addLogItem(priority = Log.ERROR, data = "Response Error: ${it.errorMsg}")
                        doCmdResponseResultError(it.cmdStr, it.errorMsg)
                    }

                    is CmdResponseResultTimeOut -> {
                        addLogItem(priority = Log.ERROR, data = "Response TimeOut")
                        doCmdResponseResultTimeOut(it.cmdStr, it.errorMsg)
                    }

                    is CmdResponseResultSuccess -> {
                        addLogItem(priority = Log.INFO, data = it.cmdResult.responseContent)
                        setResultData(it.cmdResult.responseContent)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun collectBleData() {
        launchAndRepeatWithViewLifecycle {
            bleViewModel.state.collect { state ->
                Timber.i("$fragmentName Medo BluetoothGatt: $state")
                when (state) {
                    NoDeviceState -> {}
                    is WorkingState -> when (state.result) {
                        is IdleResult,
                        is ConnectingResult -> {
                            addLogItem(priority = Log.VERBOSE, data = "Connecting")
                            showLoadingDialog(StringUtils.getString(R.string.ble_state_connecting))
                        }

                        is ConnectedResult -> {
                            addLogItem(priority = Log.INFO, data = "Connected")
                            dismissLoadingDialog()
                            onConnectionStateChanged(true)
                        }

                        is ReadyResult -> {
                            addLogItem(priority = Log.INFO, data = "Ready")
                            onBleDeviceReady()
                        }

                        is SuccessResult -> {
                            addLogItem(priority = Log.INFO, data = state.result.data.response)
                            setResultData(state.result.data.response)
                        }

                        is DisconnectedResult -> {
                            addLogItem(priority = Log.INFO, data = "Disconnected")
                            dismissLoadingDialog()
                            onConnectionStateChanged(false)
                        }

                        is LinkLossResult -> {
                            addLogItem(priority = Log.ERROR, data = "LinkLoss")
                            dismissLoadingDialog()
                            onConnectionStateChanged(false)
                        }

                        is MissingServiceResult -> {
                            addLogItem(priority = Log.ERROR, data = "MissingService")
                            dismissLoadingDialog()
                            onConnectionStateChanged(false)
                        }

                        is UnknownErrorResult -> {
                            addLogItem(priority = Log.ERROR, data = "UnknownError")
                            dismissLoadingDialog()
                        }
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

    open fun onConnectionStateChanged(isConnected: Boolean) {}
    open fun onBleDeviceReady() {}

    abstract fun setResultData(cmdStr: String)


    /**
     * 发送指令队列中的第一条指令
     */
    protected inline fun sendCommandFromCmdList(
        delaySendMillis: Long = 0,//默认不延迟发送
        isStartTimeoutJob: Boolean = false,//默认不启动超时Job
        timeoutMillis: Long = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
        crossinline block: () -> Unit = {}
    ) {
        if (commandItems.size <= 0) {
            cancelNearbyCommunicationTimeoutJob()
            block()
            return
        }

        val command = commandItems.first
        commandItems.removeFirst()
        addLogItem(priority = Log.INFO, data = command)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey, delaySendMillis)
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
        Timber.i("$fragmentName 发送指令超时")
        addLogItem(priority = Log.ERROR, data = "Response TimeOut")
        if (isShowMsg) {
            Toaster.show(msg.ifEmpty { "发送指令超时,请稍后尝试" })
        }
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
        commandItems.clear()
        refreshLayout?.finish(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutJob?.cancel() // 在Fragment销毁时取消timeoutJob
    }

    fun addLogItem(priority: Int, data: String) {
        val logInfo = LogInfo(
            sessionId = logViewModel.getLogSession()?.id ?: "",
            logLevel = LogLevel.fromPriority(priority),
            data = data,
            createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
            createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
        )
        logViewModel.insertLog(logInfo)
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