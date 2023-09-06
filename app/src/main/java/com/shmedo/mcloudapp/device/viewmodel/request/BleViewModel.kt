package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.mcloudapp.device.MedoViewState
import com.shmedo.mcloudapp.device.NoDeviceState
import com.shmedo.mcloudapp.device.WorkingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
class BleViewModel : ViewModel() {

//    val state = medoBleRepository.data.stateIn(
//        viewModelScope,
//        WhileSubscribed(5000),
//        ScanningState.Loading
//    )

    private val _state = MutableStateFlow<MedoViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        MedoBleRepository.instance.data.onEach {
            _state.value = WorkingState(it)
        }.launchIn(viewModelScope)
    }

    fun launch(device: DiscoveredBluetoothDevice) {
        MedoBleRepository.instance.launch(device)
    }

    fun disconnect() {
        MedoBleRepository.instance.release()
    }
}