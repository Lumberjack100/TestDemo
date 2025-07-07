package com.shmedo.mcloudapp.ui.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import java.util.UUID

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述：BLE 通信 ViewModel，管理连接状态和数据交换
 */
class BleViewModel(
    private val medoBleRepository: MedoBleRepository,
    loggerRepositoryImp: LoggerRepositoryImp
) : BaseRequestViewModel(loggerRepositoryImp) {

    // 连接状态流
    val connectionState = medoBleRepository.connectionState

    // 数据响应流
    val commandData = medoBleRepository.commandData


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

    /**
     * 记录连接状态变化
     */
    private fun logConnectionState(state: ConnectionState) {
        when (state) {
            ConnectionState.Connecting -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble device connecting"
                )
            }
            is ConnectionState.Initializing -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble device connected"
                )
            }
            is ConnectionState.Ready -> {
                addLogItem(
                    CommonMMKVOwner.iotDeviceLogSessionId,
                    Log.INFO,
                    "ble device ready"
                )
            }
            is ConnectionState.Disconnected -> {
                when (state.reason) {
                    ConnectionState.Disconnected.Reason.LINK_LOSS -> {
                        addLogItem(
                            CommonMMKVOwner.iotDeviceLogSessionId,
                            Log.ERROR,
                            "ble device link loss"
                        )
                    }
                    ConnectionState.Disconnected.Reason.NOT_SUPPORTED -> {
                        addLogItem(
                            CommonMMKVOwner.iotDeviceLogSessionId,
                            Log.ERROR,
                            "ble device missing service"
                        )
                    }
                    else -> {
                        addLogItem(
                            CommonMMKVOwner.iotDeviceLogSessionId,
                            Log.ERROR,
                            "ble device disconnected, reason: ${state.reason}"
                        )
                    }
                }
            }
            else -> {}
        }
    }

    /**
     * 记录数据响应
     */
    private fun logDataResponse(data: CommandData) {
        addLogItem(
            CommonMMKVOwner.iotDeviceLogSessionId,
            Log.INFO,
            "ble 响应内容: ${data.response}"
        )
    }
}