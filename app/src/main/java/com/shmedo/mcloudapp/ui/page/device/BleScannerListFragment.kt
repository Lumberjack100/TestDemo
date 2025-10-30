package com.shmedo.mcloudapp.ui.page.device

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.permission.BlePermissionNotAvailableReason
import com.shmedo.lib.ble.permission.util.BlePermissionState
import com.shmedo.lib.ble.permission.viewmodel.PermissionViewModel
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.ble.scanner.repository.ScanningState
import com.shmedo.lib.ble.scanner.viewmodel.ScannerViewModel
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentBleScannerListBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.BleScannerListViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BleScannerListFragment : BaseFragment() {
    private lateinit var binding: FragmentBleScannerListBinding
    private lateinit var mMessenger: PageMessenger
    private val mStates: BleScannerListViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()
    private val permissionViewModel: PermissionViewModel by viewModel()
    private val scannerViewModel: ScannerViewModel by viewModel()

    private var discoveredBluetoothDevice: DiscoveredBluetoothDevice? = null
    private var isFilterNameByScanningQRCode = false//是否通过扫描设备二维码来过滤查找设备
    private var scanSearchDeviceTimeoutJob: Job? = null

    override fun initViewModel() {
        mMessenger = getAppViewModel()
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
//                        showLoadingDialog(StringUtils.getString(R.string.processing))
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
                    is BlePermissionState.NotAvailable -> {
                        when (state.reason) {
                            BlePermissionNotAvailableReason.NOT_AVAILABLE -> {
                                mStates.bluetoothNotAvailable.set(true)
                                mStates.bluetoothMissPermission.set(false)
                                mStates.bluetoothDisabled.set(false)
                            }

                            BlePermissionNotAvailableReason.PERMISSION_REQUIRED -> {
                                mStates.bluetoothNotAvailable.set(false)
                                mStates.bluetoothDisabled.set(false)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    mStates.bluetoothMissPermission.set(true)
                                }
                            }

                            BlePermissionNotAvailableReason.DISABLED -> {
                                mStates.bluetoothNotAvailable.set(false)
                                mStates.bluetoothMissPermission.set(false)
                                mStates.bluetoothDisabled.set(true)
                            }
                        }
                    }

                    BlePermissionState.Available -> {
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
//            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                discoveredBluetoothDevice?.name?.let { deviceToken ->
                    DeviceHomeActivity.start(
                        mActivity,
                        DeviceInfo(
                            deviceToken = deviceToken.replaceFirst(Regex("^MD-?"), ""),
                            deviceName = deviceToken.replaceFirst(Regex("^MD-?"), ""),
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
        scannerViewModel.scannerState
            .collect { state ->
                when (state) {
                    ScanningState.Loading -> {
                        Timber.i("scannerViewModel.state: Loading")
                        if (binding.recyclerviewDevice.bindingAdapter.modelCount == 0)
                            binding.refreshLayout.showLoading(refresh = false)
                    }

                    is ScanningState.Error -> {
                        Timber.e("scannerViewModel.state: Error: ${state.errorMsg}")
                        binding.refreshLayout.showError()
                    }

                    is ScanningState.DevicesDiscovered -> {
                        Timber.i("scannerViewModel.state: DevicesDiscovered=${state.devices.size}")

                        updateDeviceList(state.devices)

                        // 处理扫码搜索逻辑
                        handleQRCodeScan(state.devices)
                    }
                }
            }
    }

    private fun updateDeviceList(devices: List<DiscoveredBluetoothDevice>) {
        if (devices.isNotEmpty()) {
            binding.refreshLayout.showContent()
        } else {
            binding.refreshLayout.showEmpty()
        }

        binding.recyclerviewDevice.models = devices
    }

    private fun handleQRCodeScan(devices: List<DiscoveredBluetoothDevice>) {
        if (isFilterNameByScanningQRCode && devices.isNotEmpty()) {
            val targetDevice = devices.firstOrNull { device ->
                device.name?.contains(mStates.keyWords.value.toString(), ignoreCase = true) == true
            }

            targetDevice?.let { device ->
                discoveredBluetoothDevice = device
                stopScanningSearchDeviceTimeoutJob()
                device.name?.let { deviceToken ->
                    deviceRequestViewModel.getDeviceDetailInfo(
                        deviceToken.replaceFirst(Regex("^MD-?"), "")
                    )
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
//        binding.refreshLayout.autoRefresh()
    }

    private fun refreshScan() = launchWithViewLifecycle {
        scannerViewModel.refresh()
        delay(1000)
        binding.refreshLayout.finish()
    }

    private fun requestPermissionForBluetooth() {
        XXPermissions.with(this)
            .permission(PermissionLists.getBluetoothScanPermission())
            .permission(PermissionLists.getBluetoothConnectPermission())
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: List<IPermission>, deniedList: List<IPermission>
                ) {
                    val allGranted = deniedList.isEmpty()
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