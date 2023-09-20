package com.shmedo.mcloudapp.device

import android.Manifest
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.permission.util.Available
import com.shmedo.lib.ble.permission.util.FeatureNotAvailableReason
import com.shmedo.lib.ble.permission.util.NotAvailable
import com.shmedo.lib.ble.permission.viewmodel.PermissionViewModel
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.ble.scanner.repository.ScanningState
import com.shmedo.lib.ble.scanner.viewmodel.ScannerViewModel
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.PermissionInterceptor
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentBleScannerListBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.BleScannerListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class BleScannerListFragment : BaseFragment() {
    private val binding: FragmentBleScannerListBinding by lazy { getBinding() as FragmentBleScannerListBinding }
    private val mStates: BleScannerListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val scannerViewModel: ScannerViewModel by viewModels()
    private val lifecycleScope by lazy { viewLifecycleOwner.lifecycleScope }

    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }
    private var discoveredBluetoothDevice: DiscoveredBluetoothDevice? = null


    private val rotationAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.ivBleScanRefresh, "rotation", 0f, 360f)
            .apply {
                duration = 500 // 设定旋转所需的时间
                repeatCount = ObjectAnimator.INFINITE // 设定无限次数的重复
                repeatMode = ObjectAnimator.RESTART
            }
    }

    /**
     * 定位需要进行检测的权限数组
     */
    private val needPermissions by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        else
            arrayOf()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_scanner_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initAdapter()
    }

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            rv.layoutManager = LinearLayoutManager(context)
            addType<DiscoveredBluetoothDevice>(R.layout.item_ble_device)
            R.id.item.onClick {
                discoveredBluetoothDevice = getModel<DiscoveredBluetoothDevice>()
                discoveredBluetoothDevice!!.name?.replaceFirst(Regex("^MD-?"), "")
                    ?.let { deviceToken ->
                        deviceRequestViewModel.getDeviceDetailInfo(deviceToken)
                    }
            }
        }
    }

    override fun initData() {

    }

    override fun createObserver() {
        launchAndRepeatWithViewLifecycle {
            permissionViewModel.bluetoothState.collectLatest { state ->
                Timber.d("bluetoothState: $state")
                when (state) {
                    is NotAvailable -> noBluetoothView(state.reason)
                    Available -> {
                        mStates.bluetoothNotAvailable.set(false)
                        mStates.bluetoothMissPermission.set(false)
                        mStates.bluetoothDisabled.set(false)
                        observeScanState()
                    }

                    else -> {}
                }
            }
        }
        deviceRequestViewModel.deviceInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<DeviceInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show("不支持此设备!${dataResult.responseStatus.errorMessage}")
                return@observe
            }
            DeviceHomeActivity.start(
                mActivity,
                dataResult.result!!,
                discoveredBluetoothDevice,
                BleConnect
            )
        }
    }

    private fun noBluetoothView(
        reason: FeatureNotAvailableReason,
    ) {
        when (reason) {
            FeatureNotAvailableReason.NOT_AVAILABLE -> {
                mStates.bluetoothNotAvailable.set(true)
                mStates.bluetoothMissPermission.set(false)
                mStates.bluetoothDisabled.set(false)
            }

            FeatureNotAvailableReason.PERMISSION_REQUIRED -> {
                mStates.bluetoothNotAvailable.set(false)
                mStates.bluetoothDisabled.set(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mStates.bluetoothMissPermission.set(true)
                }
            }

            FeatureNotAvailableReason.DISABLED -> {
                mStates.bluetoothNotAvailable.set(false)
                mStates.bluetoothMissPermission.set(false)
                mStates.bluetoothDisabled.set(true)
            }
        }
    }

    private fun observeScanState() {
        launchAndRepeatWithViewLifecycle {
            scannerViewModel.state.collectLatest { state ->
//                Timber.d("scannerViewModel.state: $state")
                when (state) {
                    ScanningState.Loading -> {
//                        mStates.scanState.set("正在扫描...")
//                        mStates.deviceCount.set(0)
                        Timber.i("scannerViewModel.state: Loading")
                    }

                    is ScanningState.Error -> {
//                        mStates.scanState.set("刷新")
                        Timber.e("scannerViewModel.state: Error")
                        mStates.deviceCount.set(0)
                    }

                    is ScanningState.DevicesDiscovered -> {
                        Timber.i("scannerViewModel.state: DevicesDiscovered")
                        mStates.deviceCount.set(state.devices.size)
                        binding.recyclerView.models = state.devices
                    }
                }
            }
        }
    }

    inner class ClickProxy {
        fun onEnableBluetoothClick() {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        fun onGrantPermissionClick() {
            requestPermissionForBluetooth()
        }

        fun onCancelSearchClick() {

        }

        fun onRefreshClick() {
            refresh()
        }
    }

    fun refresh() = lifecycleScope.launch {
        rotationAnimator.start()
        mStates.refreshing.set(true)
        mStates.scanState.set("正在扫描...")
        scannerViewModel.refresh()
        delay(5000)
        rotationAnimator.cancel()
        mStates.refreshing.set(false)
        mStates.scanState.set("刷新")
    }

    private fun requestPermissionForBluetooth() {
        XXPermissions.with(this)
            // 申请多个权限
            .permission(needPermissions)
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    grantedPermissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    permissionViewModel.refreshBluetoothPermission()
                    if (!allGranted) {
                        return
                    }
                }
            })
    }

    companion object {
        fun newInstance() = BleScannerListFragment()
    }
}