package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.PageRefreshLayout
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.core.model.DeviceInfo
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
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CmdResponseResultError
import com.shmedo.mcloudapp.model.CmdResponseResultSuccess
import com.shmedo.mcloudapp.model.CmdResponseResultTimeOut
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DispatchFailed
import com.shmedo.mcloudapp.model.DispatchSuccess
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.NoDeviceState
import com.shmedo.mcloudapp.model.WorkingState
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.NetIOTCommandViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var logViewModel: LogViewModel

    protected var refreshLayout: PageRefreshLayout? = null

    protected var productType = ProductType.UnKnown
    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null
    private var timeoutJob: Job? = null
    protected var commandItems = LinkedList<String>()
    protected var commandDescItems = LinkedList<String>()

    protected val lastCommunicationTime = MutableStateFlow(System.currentTimeMillis())

    // 更新最后通信时间
    protected fun updateLastCommunicationTime() {
        lastCommunicationTime.value = System.currentTimeMillis()
    }

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
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
    }

    @CallSuper
    override fun createObserver() {
        launchWithViewLifecycle {
            try {
                collectNetData()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        launchWithViewLifecycle {
            try {
                collectBleData()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    //<editor-fold desc="处理 4G 下发指令">
    private suspend fun collectNetData() {
        netIotCommandViewModel.cmdDispatchFlow.collect {
            when (it) {
                is DispatchFailed -> {
                    addLogItem(Log.ERROR, "DispatchFailed: ${it.errorMsg}")
                    doNetDispatchFailed(cmdStr = it.cmdStr, errorMsg = it.errorMsg)
                }

                is DispatchSuccess -> {
                    doNetDispatchSuccess(it.cmdStr)
                }

                is CmdResponseResultError -> {
                    addLogItem(Log.ERROR, "Response Error: ${it.errorMsg}")
                    doCmdResponseResultError(cmdStr = it.cmdStr, errMsg = it.errorMsg)
                }

                is CmdResponseResultTimeOut -> {
                    addLogItem(Log.ERROR, "Response TimeOut")
                    doCmdResponseResultTimeOut(cmdStr = it.cmdStr, errMsg = it.errorMsg)
                }

                is CmdResponseResultSuccess -> {
                    addLogItem(Log.INFO, it.cmdResult.responseContent)
                    setResultData(it.cmdResult.responseContent)
                }

                else -> {}
            }
        }
    }

    open fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        refreshLayout?.finish(false)
        dismissLoadingDialog()
        Toaster.show("指令发送失败: $errorMsg")
    }

    open fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult(cmdStr = cmdStr)
    }

    open fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean = true,
        isMessageDialog: Boolean = false
    ) {
        refreshLayout?.finish(false)
        dismissLoadingDialog()
        if (isShowErrMsg) {
            if (isMessageDialog)
                showMessageDialog("指令响应错误: $errMsg")
            else
                Toaster.show("指令响应错误: $errMsg")
        }
    }

    open fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean = true,
        isMessageDialog: Boolean = false
    ) {
        refreshLayout?.finish(false)
        dismissLoadingDialog()
        if (isShowErrMsg) {
            if (isMessageDialog)
                showMessageDialog(errMsg.ifEmpty { "设备未响应" })
            else
                Toaster.show(errMsg.ifEmpty { "设备未响应" })
        }
    }
    // </editor-fold>

    //<editor-fold desc="处理蓝牙下发指令">
    protected open suspend fun collectBleData() {
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
                        addLogItem(Log.INFO, "device ${bleDevice?.address} connecting")
                        showLoadingDialog(StringUtils.getString(R.string.ble_state_connecting))
                    }

                    is ConnectedResult -> {
                        addLogItem(Log.INFO, "device ${bleDevice?.address} connected")
                    }

                    is ReadyResult -> {
                        addLogItem(Log.INFO, "device ready")
                        onConnectionStateChanged(true)
                        onBleDeviceReady()
                    }

                    is SuccessResult -> {
                        addLogItem(Log.INFO, state.result.data.response)
                        setResultData(state.result.data.response)
                    }

                    is DisconnectedResult -> {
                        addLogItem(
                            Log.ERROR,
                            "device disconnected, reason: ${state.result.reason}"
                        )
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is LinkLossResult -> {
                        addLogItem(Log.ERROR, "device link loss")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is MissingServiceResult -> {
                        addLogItem(Log.ERROR, "device missing service")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is UnknownErrorResult -> {
                        addLogItem(Log.ERROR, "device unknown error")
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }
                }
            }
        }
    }

    open fun onConnectionStateChanged(isConnected: Boolean) {

    }

    open fun onBleDeviceReady() {
        dismissLoadingDialog()
    }
    // </editor-fold>

    abstract fun setResultData(cmdStr: String)

    protected fun sendBleCommand(command: String, delaySendMillis: Long = 0) {//默认不延迟发送
        //发送物联网指令
        if (command.startsWith(IOTConstants.COMMAND_HEADER)) {
            bleViewModel.sendIOTCommand(command, deviceInfo.apikey, delaySendMillis)
        } else {
            //发送MD指令 ##开头
            bleViewModel.sendMDCommand(command, delaySendMillis)
        }
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
        //指令队列为空，结束
        if (commandItems.size <= 0) {
            cancelNearbyCommunicationTimeoutJob()
            finishAction()
            return
        }
        val command = commandItems.first
        commandItems.removeFirst()
        addLogItem(Log.INFO, "发送指令: $command")

        //4G远程下发指令模式
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
            return
        }

        /**  蓝牙通信模式  start ***/
        if (isBleDisconnected()) {
            Toaster.show("蓝牙已断开，请重新连接")
            cancelNearbyCommunicationTimeoutJob()
            finishAction()
            return
        }
        //发送物联网指令
        if (command.startsWith(IOTConstants.COMMAND_HEADER))
            bleViewModel.sendIOTCommand(command, deviceInfo.apikey, delaySendMillis)
        else
            bleViewModel.sendMDCommand(command, delaySendMillis)//发送MD指令 ##开头

        //启动超时Job
        if (isStartTimeoutJob)
            startNearbyCommunicationTimeoutJob(command, timeoutMillis)
        /**  蓝牙通信模式  end ***/
    }

    /**
     * 启动蓝牙通讯/WIFI通信等近场通信超时 Job
     */
    fun startNearbyCommunicationTimeoutJob(
        cmdStr: String = "",
        timeMillis: Long = AppContants.Communication.DELAY_10000_MILLIS
    ) {
        // 启动一个新的协程作为超时Job
        timeoutJob?.cancel()
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
        refreshLayout?.finish(false)
        timeoutJob?.cancel()
        commandItems.clear()
        commandDescItems.clear()
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
    }

    /**
     * 显示近场通信超时提示
     */
    protected open fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String = "",
        isDismissLoadingDialog: Boolean = true,
        isShowErrMsg: Boolean = true,
        isMessageDialog: Boolean = false,
        errMsg: String = ""
    ) {
        Timber.i("${javaClass.simpleName} 设备未响应")
        addLogItem(Log.ERROR, "设备未响应")

        refreshLayout?.finish(false)
        timeoutJob?.cancel()
        commandItems.clear()
        commandDescItems.clear()
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
        if (isShowErrMsg) {
            if (isMessageDialog)
                showMessageDialog(errMsg.ifEmpty { "设备未响应" })
            else
                Toaster.show(errMsg.ifEmpty { "设备未响应" })
        }
    }

    protected fun isBleDisconnected() = communicateWay is BleConnect && !bleViewModel.isConnected()

    protected fun handleFailureResult(
        errMsg: String,
        isShowErrMsg: Boolean = true,
        isMessageDialog: Boolean = false
    ) {
        cancelNearbyCommunicationTimeoutJob()
        Timber.e(errMsg)
        if (isShowErrMsg) {
            if (isMessageDialog)
                showMessageDialog(errMsg)
            else
                Toaster.show(errMsg)
        }
    }

    /**
     * 重启设备指令
     */
    protected open fun reboot() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.REBOOT)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 恢复出厂设置指令
     */
    protected open fun restoreFactory() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.RESET)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    fun addLogItem(priority: Int, data: String) {
        logViewModel.insertLog(
            getLogItem(
                sessionId = CommonMMKVOwner.iotDeviceLogSessionId,
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
        timeoutJob?.cancel() // 在Fragment销毁时取消timeoutJob
        super.onDestroy()
    }

    companion object {
        fun newBundleArguments(
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}