package com.shmedo.mcloudapp.device.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.viewModels
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
import com.shmedo.lib.core.util.PermissionInterceptor
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentBleScannerListBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.BleScannerListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class BleScannerListFragment : BaseFragment() {
    private val binding: FragmentBleScannerListBinding by lazy { getBinding() as FragmentBleScannerListBinding }
    private val mStates: BleScannerListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val scannerViewModel: ScannerViewModel by viewModels()

    private val scanResultList = mutableListOf<DiscoveredBluetoothDevice>()
    private var discoveredBluetoothDevice: DiscoveredBluetoothDevice? = null


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
        initRefresh()
    }

    private fun initAdapter() {
        binding.recyclerviewDevice.setup { rv ->
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

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            refreshScan()
        }
    }

    override fun createObserver() {
        launchAndRepeatWithViewLifecycle {
            permissionViewModel.bluetoothState.collectLatest { state ->
                Timber.d("bluetoothState: $state")
                when (state) {
                    is NotAvailable -> {
                        when (state.reason) {
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

                    Available -> {
                        mStates.bluetoothNotAvailable.set(false)
                        mStates.bluetoothMissPermission.set(false)
                        mStates.bluetoothDisabled.set(false)
                        processScanResult()
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
        mStates.keyWords.observe(viewLifecycleOwner) { keyword ->
            Timber.i("keyWords 触发")
            if (keyword.isEmpty()) {
                binding.recyclerviewDevice.models = scanResultList
                return@observe
            }
            val filterList = scanResultList.filter {
                it.name?.contains(keyword, ignoreCase = true) == true
            }
//            binding.recyclerviewDevice.setDifferModels(filterList, false)
            binding.recyclerviewDevice.models = filterList
        }
    }

    private suspend fun processScanResult() {
        scannerViewModel.scannerState.collectLatest { state ->
            when (state) {
                ScanningState.Loading -> {
                    Timber.i("scannerViewModel.state: Loading")
                }

                is ScanningState.Error -> {
                    Timber.e("scannerViewModel.state: Error")
                }

                is ScanningState.DevicesDiscovered -> {
                    Timber.i("scannerViewModel.state: DevicesDiscovered=${state.devices.size}")
                    scanResultList.clear()
                    scanResultList.addAll(state.devices)
                    mStates.keyWords.value = mStates.keyWords.value
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
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun refreshScan() = launchWithViewLifecycle {
        scannerViewModel.refresh()
        delay(3000)
        binding.refreshLayout.finish()
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