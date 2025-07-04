package com.shmedo.mcloudapp.ui.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.communicate.service.base.BleManagerResult
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.IdleResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.ReadyResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
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
class BleViewModel(
    private val medoBleRepository: MedoBleRepository,
    loggerRepositoryImp: LoggerRepositoryImp
) :
    BaseRequestViewModel(loggerRepositoryImp) {

    private val _data = MutableSharedFlow<BleManagerResult<CommandData>>()
    val data = _data.asSharedFlow()


    init {
        medoBleRepository.data.onEach {
            logResult(it)
            _data.emit(it)
        }.launchIn(viewModelScope)
    }

    fun launch(device: DiscoveredBluetoothDevice) {
        medoBleRepository.launch(device)
    }

    fun disconnect() {
        medoBleRepository.disconnect()
    }

    fun isConnected(): Boolean {
        return medoBleRepository.isConnected()
    }

    fun sendIOTCommand(
        cmdStr: String,
        apiKey: String = "",
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            val command = if (!cmdStr.contains("&apikey=")) {
                cmdStr.plus(
                    "&apikey=${apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" }}&msgid=${
                        UUID.randomUUID().toString().substring(30)
                    }"
                )
            } else cmdStr

            medoBleRepository.sendData(command + MDConstants.COMMAND_FOOTER)
        }
    }

    fun sendMDCommand(
        cmdStr: String,
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            val command = if (!cmdStr.endsWith(MDConstants.COMMAND_FOOTER)) {
                cmdStr.plus(MDConstants.COMMAND_FOOTER)
            } else cmdStr
            medoBleRepository.sendData(command)
        }
    }

    private fun logResult(result: BleManagerResult<CommandData>) {
        when (result) {
            is IdleResult, is ConnectingResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble device connecting"
                )
            }

            is ConnectedResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble device  connected"
                )
            }

            is ReadyResult -> {
                addLogItem(CommonMMKVOwner.iotDeviceLogSessionId, Log.INFO, "ble device ready")
            }

            is SuccessResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble 响应内容: ${result.data.response}"
                )
            }

            is DisconnectedResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.ERROR,
                    "ble device disconnected, reason: ${result.reason}"
                )
            }

            is LinkLossResult -> {
                addLogItem(CommonMMKVOwner.iotDeviceLogSessionId, Log.ERROR, "ble device link loss")
            }

            is MissingServiceResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.ERROR,
                    "ble device missing service"
                )
            }

            is UnknownErrorResult -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.ERROR,
                    "ble device unknown error"
                )
            }
        }
    }

}