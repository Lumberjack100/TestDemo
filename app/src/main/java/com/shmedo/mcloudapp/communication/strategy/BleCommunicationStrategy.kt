package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.communicate.data.BleCommandSession
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.data.SessionPriority
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.session.CommandPriority
import com.shmedo.mcloudapp.ui.viewmodel.request.BleViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import timber.log.Timber

/**
 * 蓝牙通信策略实现
 * 集成现有的BleViewModel进行蓝牙通信
 */
class BleCommunicationStrategy(
    private val bleViewModel: BleViewModel,
    private val deviceInfo: DeviceInfo
) : CommunicationStrategy {

    override fun sendCommand(command: String, config: CommandConfig): Flow<CommandResult> {
        val session = createBleSession(config)
        val responseChannel = Channel<CommandData>(1)

        return flow {
            // 检查蓝牙连接状态
            if (!isConnected()) {
                emit(CommandResult.Error(DeviceError.Disconnected("蓝牙"), command))
                return@flow
            }

            Timber.i("BLE发送指令: $command")

            // 延迟发送（如果配置了延迟）
            if (config.delayBeforeSend > 0) {
                delay(config.delayBeforeSend)
            }

            // 注册会话监听器
            bleViewModel.registerSessionListener(session.sessionId) { commandData ->
                if (commandData.response.startsWith("TIMEOUT:")) {
                    return@registerSessionListener
                }
                responseChannel.trySend(commandData)
            }

            // 发送会话指令
            sendBleSessionCommand(command, session)

            // 等待响应
            withTimeout(config.timeout) {
                val data = responseChannel.receive()
                Timber.i("BLE会话响应内容: ${data.response}")
                emit(CommandResult.Success(data.response, command))
            }

        }.onCompletion { cause ->
            // 无论流程如何结束（成功、异常、取消），都会执行清理
            Timber.d("流程结束清理BLE会话资源: ${session.sessionId}, 原因: $cause")
            bleViewModel.unregisterSessionListener(session.sessionId)
            responseChannel.close()

        }.catch { e ->
            when (e) {
                is TimeoutCancellationException -> {
                    emit(CommandResult.Timeout(command, config.timeout))
                }

                else -> {
                    Timber.e(e, "蓝牙通信异常")
                    emit(
                        CommandResult.Error(
                            error = DeviceError.Bluetooth(e.message ?: "未知蓝牙错误"),
                            command = command
                        )
                    )
                }
            }
        }
    }


    /**
     * 创建BLE会话
     */
    private fun createBleSession(config: CommandConfig): BleCommandSession {
        return BleCommandSession(
            sessionId = "ble_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}",
            ownerId = config.ownerId ?: "unknown",
            priority = when (config.priority) {
                CommandPriority.URGENT -> SessionPriority.URGENT
                CommandPriority.HIGH -> SessionPriority.HIGH
                CommandPriority.NORMAL -> SessionPriority.NORMAL
                CommandPriority.LOW -> SessionPriority.LOW
            }
        )
    }

    /**
     * 发送会话蓝牙指令
     */
    private fun sendBleSessionCommand(command: String, session: BleCommandSession) {
        // 根据指令类型选择发送方式
        if (command.startsWith(IOTConstants.COMMAND_HEADER)) {
            // 发送IOT指令
            bleViewModel.sendIOTSessionCommand(command, session, deviceInfo.apikey)
        } else {
            // 发送MD指令
            bleViewModel.sendMDSessionCommand(command, session)
        }
    }

    override fun isConnected(): Boolean {
        return bleViewModel.isConnected()
    }

    override fun getConnectionState(): Flow<DeviceConnectionState> {
        return bleViewModel.connectionState.map { state ->
            when (state) {
                ConnectionState.Connecting -> DeviceConnectionState.Connecting
                is ConnectionState.Initializing -> DeviceConnectionState.Connecting
                is ConnectionState.Ready -> DeviceConnectionState.Connected
                is ConnectionState.Disconnected -> DeviceConnectionState.Disconnected
                else -> DeviceConnectionState.Disconnected
            }
        }
    }

    override fun getStrategyType(): String = "蓝牙"

    override fun cleanup() {
        // 清理蓝牙相关资源
        // BleViewModel 的清理由其自身生命周期管理
    }

    override fun addDeviceLogItem(priority: Int, data: String) {
        bleViewModel.addLogItem(
            sessionId = CommonMMKVOwner.iotDeviceLogSessionId, priority = priority, data = data
        )
    }

} 