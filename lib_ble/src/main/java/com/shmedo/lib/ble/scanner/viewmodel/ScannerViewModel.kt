package com.shmedo.lib.ble.scanner.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.scanner.repository.DevicesScanFilter
import com.shmedo.lib.ble.scanner.repository.ScannerRepository
import com.shmedo.lib.ble.scanner.repository.ScanningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
private const val FILTER_RSSI = -50 // [dBm]

class ScannerViewModel : ViewModel() {
    private var uuid: ParcelUuid? = null

    private val filterConfig = MutableStateFlow(
        DevicesScanFilter(
            filterUuidRequired = true,
            filterNearbyOnly = false,
            filterWithNames = true
        )
    )

    val state = filterConfig
        .combine(ScannerRepository.instance.getScannerState()) { config, result ->
            when (result) {
                is ScanningState.DevicesDiscovered -> result.applyFilters(config)
                else -> result
            }
        } .stateIn(
            viewModelScope,
            WhileSubscribed(5000),
            ScanningState.Loading
        )

    // This can't be observed in View Model Scope, as it can exist even when the
    // scanner is not visible. Scanner state stops scanning when it is not observed.
    // .stateIn(viewModelScope, SharingStarted.Lazily, ScanningState.Loading)

    private fun ScanningState.DevicesDiscovered.applyFilters(config: DevicesScanFilter) =
        ScanningState.DevicesDiscovered(devices
            .filter {
                uuid == null ||
                        config.filterUuidRequired == false ||
                        it.scanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
            }
            .filter { !config.filterNearbyOnly || it.highestRssi >= FILTER_RSSI }
            .filter { !config.filterWithNames || it.hadName }
        )

    fun setFilterUuid(uuid: ParcelUuid?) {
        this.uuid = uuid
        if (uuid == null) {
            filterConfig.value = filterConfig.value.copy(filterUuidRequired = null)
        }
    }

    fun setFilter(config: DevicesScanFilter) {
        this.filterConfig.value = config
    }

    fun refresh() {
        ScannerRepository.instance.clear()
    }

    override fun onCleared() {
        super.onCleared()
        ScannerRepository.instance.clear()
    }
}