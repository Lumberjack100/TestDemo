package com.shmedo.lib.ble.scanner.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.scanner.model.DevicesScanFilter
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.ble.scanner.model.SortMode
import com.shmedo.lib.ble.scanner.repository.ScannerRepository
import com.shmedo.lib.ble.scanner.repository.ScanningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


private const val FILTER_RSSI = -50 // [dBm]

class ScannerViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private var uuid: ParcelUuid? = null
    private var deviceName: String = ""

    private val filterConfig = MutableStateFlow(
        DevicesScanFilter(
            filterUuidRequired = true,
            filterNearbyOnly = false,
            filterWithNames = true
        )
    )

    val scannerState = filterConfig
        .combine(scannerRepository.getScannerState()) { config, result ->
            when (result) {
                is ScanningState.DevicesDiscovered -> result.applyFilters(config)
                else -> result
            }
        }
        .stateIn(
            viewModelScope,
            WhileSubscribed(5000),
            ScanningState.Loading
        )
    // This can't be observed in View Model Scope, as it can exist even when the
    // scanner is not visible. Scanner state stops scanning when it is not observed.
    // .stateIn(viewModelScope, SharingStarted.Lazily, ScanningState.Loading)

    //优化后
    private fun ScanningState.DevicesDiscovered.applyFilters(config: DevicesScanFilter): ScanningState.DevicesDiscovered {
        val filteredDevices = devices
            .asSequence() // 使用序列优化性能
            .filter { device ->
                // UUID 过滤
                uuid == null ||
                        config.filterUuidRequired == false ||
                        device.scanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
            }
            .filter { device ->
                // RSSI 过滤
                !config.filterNearbyOnly || device.highestRssi >= FILTER_RSSI
            }
            .filter { device ->
                // 名称过滤
                !config.filterWithNames ||
                        (device.hadName && (deviceName.isEmpty() ||
                                device.name?.contains(deviceName, ignoreCase = true) == true))
            }
            .toList()

        val sorted = when (config.sortMode) {
            SortMode.ByRssiDesc -> // RSSI 数值越大代表信号越强，额外使用最高 RSSI 与设备标识保证排序稳定
                filteredDevices.sortedWith(
                    compareByDescending<DiscoveredBluetoothDevice> { it.rssi }
                        .thenByDescending { it.highestRssi }
                        .thenBy { it.displayNameOrAddress }
                )
            SortMode.None -> filteredDevices
        }

        return ScanningState.DevicesDiscovered(sorted)
    }

    fun setFilterUuid(uuid: ParcelUuid?) {
        this.uuid = uuid
        if (uuid == null) {
            filterConfig.value = filterConfig.value.copy(filterUuidRequired = null)
        }
    }

    // 优化设置过滤器名称的方法
    fun setFilterName(name: String = "") {
        if (deviceName != name) {
            deviceName = name
            filterConfig.value = filterConfig.value.copy(filterWithNames = name.isNotEmpty())
        }
    }


    fun setFilter(config: DevicesScanFilter) {
        this.filterConfig.value = config
    }

    fun refresh() {
        scannerRepository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        scannerRepository.clear()
    }
}