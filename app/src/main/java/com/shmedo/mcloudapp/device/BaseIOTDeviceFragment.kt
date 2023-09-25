package com.shmedo.mcloudapp.device

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
import com.shmedo.mcloudapp.common.ext.dismissWaitDialog
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.showWaitDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.NetIOTCommandViewModel
import timber.log.Timber

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
                        dismissWaitDialog()
                        doDispatchFailed(it.cmdStr, it.errorMsg)
                    }

                    is DispatchSuccess -> {
                        doDispatchSuccess(it.cmdStr)
                    }

                    is CmdResponseResultError -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应错误: ${it.errorMsg}")
                    }

                    is CmdResponseResultTimeOut -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应超时")
                    }

                    is CmdResponseResultSuccess -> {
//                        dismissWaitDialog()
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
                            showWaitDialog(StringUtils.getString(R.string.ble_state_connecting))
                        }

                        is ConnectedResult -> {
                            dismissWaitDialog()
                            onConnectionStateChanged(true)
                        }

                        is ReadyResult -> {
                            onBleDeviceReady()
                        }

                        is SuccessResult -> {
                            setResultData(state.result.data.response)
                        }

                        is DisconnectedResult -> {
                            dismissWaitDialog()
                            onConnectionStateChanged(false)
                        }

                        is LinkLossResult -> {
                            dismissWaitDialog()
                            onConnectionStateChanged(false)
                        }

                        is MissingServiceResult -> {
                            dismissWaitDialog()
                            onConnectionStateChanged(false)
                        }

                        is UnknownErrorResult -> {
                            dismissWaitDialog()
                        }
                    }
                }
            }
        }
    }

    open fun onConnectionStateChanged(isConnected: Boolean) {

    }

    open fun onBleDeviceReady() {

    }

    abstract fun doDispatchFailed(cmdStr: String, errorMsg: String)
    abstract fun doDispatchSuccess(cmdStr: String)
    abstract fun setResultData(cmdStr: String)
}