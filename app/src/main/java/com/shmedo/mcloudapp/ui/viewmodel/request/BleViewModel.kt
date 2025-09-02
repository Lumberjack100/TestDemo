package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.ble.communicate.data.BleCommandSession
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.data.SessionCommand
import com.shmedo.lib.ble.communicate.service.MedoBleRepository
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // ==================== 新的会话指令发送方法 ====================

    /**
     * 发送会话IOT指令
     */
    fun sendIOTSessionCommand(
        command: String,
        session: BleCommandSession,
        apiKey: String = "",
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            val enhancedCommand = if (!command.contains("&apikey=")) {
                command.plus(
                    "&apikey=${apiKey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" }}&msgid=${
                        UUID.randomUUID().toString().substring(30)
                    }"
                )
            } else command

            val sessionCommand = SessionCommand(
                command = enhancedCommand + MDConstants.COMMAND_FOOTER,
                session = session,
                timeoutMs = 10000
            )

            medoBleRepository.sendSessionCommand(sessionCommand)
        }
    }

    /**
     * 发送会话MD指令
     */
    fun sendMDSessionCommand(
        command: String,
        session: BleCommandSession,
        timeMillis: Long = 0
    ) {
        viewModelScope.launch {
            delay(timeMillis)
            val enhancedCommand = if (!command.endsWith(MDConstants.COMMAND_FOOTER)) {
                command.plus(MDConstants.COMMAND_FOOTER)
            } else command

            val sessionCommand = SessionCommand(
                command = enhancedCommand,
                session = session,
                timeoutMs = 10000
            )

            medoBleRepository.sendSessionCommand(sessionCommand)
        }
    }


    /**
     * 注册会话监听器
     */
    suspend fun registerSessionListener(sessionId: String, listener: (CommandData) -> Unit) {
        medoBleRepository.registerSessionListener(sessionId, listener)
    }

    /**
     * 注销会话监听器
     */
    suspend fun unregisterSessionListener(sessionId: String) {
        medoBleRepository.unregisterSessionListener(sessionId)
    }

    /**
     * 取消会话
     */
    fun cancelSession(sessionId: String) {
        medoBleRepository.cancelSession(sessionId)
    }

    /**
     * 清理过期会话
     */
    fun cleanupExpiredSessions() {
        medoBleRepository.cleanupExpiredSessions()
    }

    /**
     * 获取会话状态
     */
    fun getSessionStatus(): Triple<Int, Int, Int>? {
        return medoBleRepository.getSessionStatus()
    }
}