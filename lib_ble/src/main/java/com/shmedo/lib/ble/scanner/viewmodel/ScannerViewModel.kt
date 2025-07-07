package com.shmedo.lib.ble.scanner.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.scanner.model.DevicesScanFilter
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


//    private fun ScanningState.DevicesDiscovered.applyFilters(config: DevicesScanFilter) =
//        ScanningState.DevicesDiscovered(
//            devices
//            .filter {
//                uuid == null ||
//                        config.filterUuidRequired == false ||
//                        it.scanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
//            }
//            .filter { !config.filterNearbyOnly || it.highestRssi >= FILTER_RSSI }
//            .filter {
//                !config.filterWithNames || (it.hadName && (it.name?.contains(deviceName) ?: true))
//            }
//        )

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
                        (device.hadName && (deviceName.isEmpty() || device.name?.contains(
                            deviceName,
                            ignoreCase = true
                        ) == true))
            }
//            .sortedByDescending { it.highestRssi } // 按信号强度排序
            .toList()

        val result = ScanningState.DevicesDiscovered(filteredDevices)

        return result
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