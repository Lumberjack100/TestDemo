package com.shmedo.mcloudapp.device.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.permission.util.Available
import com.shmedo.lib.ble.permission.util.FeatureNotAvailableReason
import com.shmedo.lib.ble.permission.util.NotAvailable
import com.shmedo.lib.ble.permission.viewmodel.PermissionViewModel
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.ble.scanner.repository.ScanningState
import com.shmedo.lib.ble.scanner.viewmodel.ScannerViewModel
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.permission.PermissionInterceptor
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentBleScannerListBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.BleScannerListViewModel
import com.shmedo.mcloudapp.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

class BleScannerListFragment : BaseFragment() {
    private lateinit var binding: FragmentBleScannerListBinding
    private lateinit var mMessenger: PageMessenger
    private lateinit var mStates: BleScannerListViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private lateinit var permissionViewModel: PermissionViewModel
    private lateinit var scannerViewModel: ScannerViewModel

    private var discoveredBluetoothDevice: DiscoveredBluetoothDevice? = null
    private var isFilterNameByScanningQRCode = false//是否通过扫描设备二维码来过滤查找设备
    private var scanSearchDeviceTimeoutJob: Job? = null

    /**
     * 需要进行检测的权限数组
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

    override fun initViewModel() {
        mMessenger = getAppViewModel()
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
        permissionViewModel = getViewModel()
        scannerViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_scanner_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleScannerListBinding
        initAdapter()
        initRefresh()
    }

    private fun initAdapter() {
        binding.recyclerviewDevice.setup { rv ->
            rv.layoutManager = LinearLayoutManager(context)
            addType<DiscoveredBluetoothDevice>(R.layout.item_ble_device)
            R.id.item.onClick {
                discoveredBluetoothDevice = getModel<DiscoveredBluetoothDevice>()
                discoveredBluetoothDevice?.name?.replaceFirst(Regex("^MD-?"), "")
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
            permissionViewModel.bluetoothState.collect { state ->
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
                        launchAndRepeatWithViewLifecycle {
                            processScanResult()
                        }
                    }

                    else -> {}
                }
            }
        }

        mStates.keyWords.observe(viewLifecycleOwner) { keyword ->
            Timber.i("keyWords 触发")
            scannerViewModel.setFilterName(keyword)
        }
        mMessenger.scanSNResult.observe(viewLifecycleOwner) { sn ->
            if (sn.isNullOrEmpty()) {
                return@observe
            }
            scannerViewModel.setFilterName(sn)
            isFilterNameByScanningQRCode = true
            mStates.keyWords.value = sn
            startScanningSearchDeviceTimeoutJob()
        }
        deviceRequestViewModel.deviceInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<DeviceInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                discoveredBluetoothDevice?.name?.let { token ->
                    //CG0 自组网报警网关 特殊处理
                    if (!token.endsWith(ProductType.COLLECTOR_G_0.newSuffix)) {
                        showMessageDialog("获取设备信息失败!${dataResult.responseStatus.errorMessage}")
                        return@observe
                    }
                    DeviceHomeActivity.start(
                        mActivity,
                        DeviceInfo(
                            productName = ProductType.COLLECTOR_G_0.productName,
                            deviceToken = token,
                            deviceName = "MD-GW100",
                        ),
                        discoveredBluetoothDevice,
                        BleConnect
                    )
                    return@observe
                }
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

    private suspend fun processScanResult() {
        scannerViewModel.scannerState.collect { state ->
            when (state) {
                ScanningState.Loading -> {
                    Timber.i("scannerViewModel.state: Loading")
                }

                is ScanningState.Error -> {
                    Timber.e("scannerViewModel.state: Error")
                }

                is ScanningState.DevicesDiscovered -> {
                    Timber.i("scannerViewModel.state: DevicesDiscovered=${state.devices.size}")
                    if (state.devices.isNotEmpty())
                        binding.refreshLayout.showContent()
                    else
                        binding.refreshLayout.showEmpty()
                    binding.recyclerviewDevice.models = state.devices

                    if (isFilterNameByScanningQRCode && state.devices.isNotEmpty()) {
                        discoveredBluetoothDevice = state.devices[0]
                        discoveredBluetoothDevice?.name?.let { deviceToken ->
                            if (deviceToken.contains(mStates.keyWords.value.toString())) {
                                stopScanningSearchDeviceTimeoutJob()
                                deviceRequestViewModel.getDeviceDetailInfo(
                                    deviceToken.replaceFirst(
                                        Regex("^MD-?"),
                                        ""
                                    )
                                )
                            }
                        }
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
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun refreshScan() = launchWithViewLifecycle {
        scannerViewModel.refresh()
        delay(1000)
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
                    if (!allGranted) {
                        return
                    }
                    permissionViewModel.refreshBluetoothPermission()
                }
            })
    }

    private fun startScanningSearchDeviceTimeoutJob() {
        scanSearchDeviceTimeoutJob?.cancel()
        scanSearchDeviceTimeoutJob = launchWithViewLifecycle {
            withContext(Dispatchers.Main) {
                showLoadingDialog("搜索设备 ${mStates.keyWords.value} 蓝牙广播中...")
            }
            delay(5000)
            withContext(Dispatchers.Main) {
                dismissLoadingDialog()
                isFilterNameByScanningQRCode = false
                mStates.keyWords.value = "MD"
                delay(500)
                showMessageDialog("未搜索到该设备!")
            }
        }
    }

    private fun stopScanningSearchDeviceTimeoutJob() {
        isFilterNameByScanningQRCode = false
        mStates.keyWords.value = "MD"
        scanSearchDeviceTimeoutJob?.cancel()
        dismissLoadingDialog()
    }

    companion object {
        fun newInstance() = BleScannerListFragment()
    }
}