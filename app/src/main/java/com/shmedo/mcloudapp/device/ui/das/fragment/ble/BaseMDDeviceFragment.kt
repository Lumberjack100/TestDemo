package com.shmedo.mcloudapp.device.ui.das.fragment.ble

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
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
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.addIOTDeviceLogItem
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.device.common.NoDeviceState
import com.shmedo.mcloudapp.device.common.WorkingState
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.LinkedList

/**
 * 创建者：gonghe
 * 创建时间：2024/4/11
 * 描述： TODO
 */
abstract class BaseMDDeviceFragment: BaseFragment()  {
    protected lateinit var mMessenger: PageMessenger
    protected lateinit var bleViewModel: BleViewModel
    protected lateinit var logViewModel: LogViewModel

    protected var refreshLayout: PageRefreshLayout? = null

    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null
    private var timeoutJob: Job? = null

    protected var commandItems = LinkedList<String>()
    protected var commandDescItems = LinkedList<String>()

    @CallSuper
    override fun initViewModel() {
        mMessenger = getAppViewModel()
        bleViewModel = getFragmentScopeViewModel()
        logViewModel = getActivityScopeViewModel()
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
            collectBleData()
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
                        showLoadingDialog(StringUtils.getString(R.string.ble_state_connecting))
                    }

                    is ConnectedResult -> {
//                        dismissLoadingDialog()
//                        onConnectionStateChanged(true)
                    }

                    is ReadyResult -> {
                        onConnectionStateChanged(true)
                        onBleDeviceReady()
                    }

                    is SuccessResult -> {
                        addIOTDeviceLogItem(
                            priority = Log.INFO,
                            data = state.result.data.response,
                            logViewModel.viewModelScope
                        )
                        setResultData(state.result.data.response)
                    }

                    is DisconnectedResult -> {
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is LinkLossResult -> {
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is MissingServiceResult -> {
                        dismissLoadingDialog()
                        onConnectionStateChanged(false)
                    }

                    is UnknownErrorResult -> {
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

    abstract fun setResultData(cmdStr: String)

    /**
     * 保存配置信息，但不会重启设备指令
     */
//    protected fun saveConfigInfoNoReboot() {
//        val command: String = MDCommandUtil
//            .getCommand(MDCommandType.SAVE_CONFIG_INFO, SaveConfigMode.SAVE_NO_REBOOT.toString())
//        Timber.d("发送保存配置不重启设备指令===%s", command)
//    }

    /**
     * 发送调试指令
     */
    protected fun sendDebugCommand(
        command: String
    ) {
        addIOTDeviceLogItem(priority = Log.INFO, data = command, logViewModel.viewModelScope)
        bleViewModel.sendIOTCommand(command, false, deviceInfo.apikey)
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected inline fun sendMDCommandFromCmdList(
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
        addIOTDeviceLogItem(priority = Log.INFO, data = command, logViewModel.viewModelScope)

        bleViewModel.sendMDCommand(command, delaySendMillis)
        if (isStartTimeoutJob)
            startNearbyCommunicationTimeoutJob(command, timeoutMillis)
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
        commandDescItems.clear()
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
        addIOTDeviceLogItem(
            priority = Log.ERROR,
            data = "Response TimeOut",
            logViewModel.viewModelScope
        )

        timeoutJob?.cancel()
        if (isShowMsg) {
            Toaster.show(msg.ifEmpty { "发送指令超时,请稍后尝试" })
        }
        if (isDismissLoadingDialog) {
            dismissLoadingDialog()
        }
        commandItems.clear()
        commandDescItems.clear()
        refreshLayout?.finish(false)
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

    /**
     *
     */
    override fun isRestrictHiddenMode(): Boolean {
        return true
    }
}