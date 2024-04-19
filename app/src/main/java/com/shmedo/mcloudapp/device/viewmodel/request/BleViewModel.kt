package com.shmedo.mcloudapp.device.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.viewmodel.BaseRequestViewModel
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import com.shmedo.mcloudapp.device.common.MedoViewState
import com.shmedo.mcloudapp.device.common.WorkingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
class BleViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) :
    BaseRequestViewModel(loggerRepositoryImp) {
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
        MedoBleRepository.instance.disconnect()
    }

    fun isConnected(): Boolean {
        return MedoBleRepository.instance.isConnected()
    }

    fun sendIOTCommand(
        cmdStr: String,
        needApiKey: Boolean = false,
        apiKey: String = "",
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            val command = if (needApiKey) {
                cmdStr.plus(
                    "&apikey=${apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" }}&msgid=${
                        UUID.randomUUID().toString().substring(30)
                    }"
                )
            } else cmdStr

            MedoBleRepository.instance.sendData(command + "\r\n")
        }
    }

    fun sendMDCommand(
        cmdStr: String,
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            MedoBleRepository.instance.sendData(cmdStr)
        }
    }
}