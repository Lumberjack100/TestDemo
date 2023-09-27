package com.shmedo.mcloudapp.device

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
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
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
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
    protected val mMessenger: PageMessenger by lazy { getAppViewModel() }
    protected val netIotCommandViewModel: NetIOTCommandViewModel by viewModels()
    protected val bleViewModel: BleViewModel by activityViewModels()
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
        if (communicateWay is NetPlatformConnect) {
            processNetPlatform()
        } else {
            processBle()
        }
    }

    private fun processNetPlatform() {
        launchAndRepeatWithViewLifecycle {
            netIotCommandViewModel.cmdDispatchFlow.collect {
                when (it) {
                    is DispatchFailed -> {
                        dismissLoadingDialog()
                        doNetDispatchFailed(it.cmdStr, it.errorMsg)
                    }

                    is DispatchSuccess -> {
                        doNetDispatchSuccess(it.cmdStr)
                    }

                    is CmdResponseResultError -> {
                        dismissLoadingDialog()
                        Toaster.show("指令响应错误: ${it.errorMsg}")
                    }

                    is CmdResponseResultTimeOut -> {
                        dismissLoadingDialog()
                        Toaster.show("指令响应超时")
                    }

                    is CmdResponseResultSuccess -> {
//                        dismissLoadingDialog
                        setResultData(it.cmdResult.responseContent)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun processBle() {
        launchAndRepeatWithViewLifecycle {
            bleViewModel.state.collect { state ->
                Timber.i("Medo BluetoothGatt: $state")
                when (state) {
                    NoDeviceState -> {}
                    is WorkingState -> when (state.result) {
                        is IdleResult,
                        is ConnectingResult -> {
                            showLoadingDialog(StringUtils.getString(R.string.ble_state_connecting))
                        }

                        is ConnectedResult -> {
                            dismissLoadingDialog()
                            onConnectionStateChanged(true)
                        }

                        is ReadyResult -> {
                            onBleDeviceReady()
                        }

                        is SuccessResult -> {
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
                        }
                    }
                }
            }
        }
    }

    open fun onConnectionStateChanged(isConnected: Boolean) {}
    open fun onBleDeviceReady() {}

    abstract fun doNetDispatchFailed(cmdStr: String, errorMsg: String)
    abstract fun doNetDispatchSuccess(cmdStr: String)
    abstract fun setResultData(cmdStr: String)

    protected open fun showTimeoutAlert() {}
    protected fun startTimeoutJob(timeMillis: Long = AppContants.Communication.DELAY_10000_MILLIS) {
        // 启动一个新的协程作为超时Job
        timeoutJob = launchWithViewLifecycle {
            delay(timeMillis) // 延迟10秒
            withContext(Dispatchers.Main) {
                showTimeoutAlert()
            }
        }
    }

    @CallSuper
    protected open fun cancelTimeoutJob() {
        timeoutJob?.cancel()
    }


    /**
     * 发送指令队列中的第一条指令
     */
    protected inline fun sendCommandFromCmdList(crossinline block: () -> Unit = {}) {
        if (commandItems.size > 0) {
            val command = commandItems.getFirst()
            if (communicateWay is NetPlatformConnect) {
                netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
            } else {
                bleViewModel.sendCommand(command, true, deviceInfo.apiKey, 1000)
            }
            commandItems.removeFirst()
        } else {
            cancelTimeoutJob()
            block()
        }
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