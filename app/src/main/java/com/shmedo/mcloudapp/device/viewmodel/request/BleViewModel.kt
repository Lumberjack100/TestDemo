package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.mcloudapp.device.MedoViewState
import com.shmedo.mcloudapp.device.WorkingState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    //    private val _state = MutableStateFlow<MedoViewState>(NoDeviceState)
//    val state = _state.asStateFlow()
    private val _state: MutableSharedFlow<MedoViewState> = MutableSharedFlow()
    val state = _state.asSharedFlow()



    init {
        MedoBleRepository.instance.data.onEach {
//            _state.value = WorkingState(it)
            _state.emit(WorkingState(it))
        }.launchIn(viewModelScope)
    }

    fun launch(device: DiscoveredBluetoothDevice) {
        MedoBleRepository.instance.launch(device)
    }

    fun disconnect() {
        MedoBleRepository.instance.release()
    }
}