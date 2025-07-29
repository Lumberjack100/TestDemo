package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.ui.viewmodel.request.BleViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

/**
 * 蓝牙通信策略实现
 * 集成现有的BleViewModel进行蓝牙通信
 */
class BleCommunicationStrategy(
    private val bleViewModel: BleViewModel,
    private val deviceInfo: DeviceInfo
) : CommunicationStrategy {

    override suspend fun sendCommand(command: String, config: CommandConfig): Flow<CommandResult> = 
        flow {
            // 检查蓝牙连接状态
            if (!isConnected()) {
                emit(CommandResult.Error(
                    error = DeviceError.Disconnected("蓝牙"),
                    command = command
                ))
                return@flow
            }

            Timber.i("BLE发送指令: $command")
            
            // 延迟发送（如果配置了延迟）
            if (config.delayBeforeSend > 0) {
                delay(config.delayBeforeSend)
            }

            // 发送指令
            sendBleCommand(command)

            // 使用 timeout 操作符更合适
            bleViewModel.commandData
                .timeout(config.timeout.milliseconds)
                .take(1) // 只取第一个响应
                .collect { data ->
                    Timber.i("BLE响应内容: ${data.response}")
                    emit(CommandResult.Success(
                        responseData = data.response,
                        command = command
                    ))
                }
        }.catch { e ->
            // 使用Flow.catch处理异常，避免Flow异常透明度违规
            when (e) {
                is TimeoutCancellationException -> {
                    emit(CommandResult.Timeout(command, config.timeout))
                }
                is CancellationException -> {
                    // Flow被取消，不发出任何值，避免异常透明度违规
                    Timber.d("BLE指令被取消: $command")
                }
                else -> {
                    // 其他异常
                    Timber.e(e, "蓝牙通信异常")
                    emit(CommandResult.Error(
                        error = DeviceError.Bluetooth(e.message ?: "未知蓝牙错误"),
                        command = command
                    ))
                }
            }
        }

    /**
     * 发送蓝牙指令
     */
    private fun sendBleCommand(command: String) {
        // 根据指令类型选择发送方式
        if (command.startsWith(IOTConstants.COMMAND_HEADER)) {
            // 发送IOT指令
            bleViewModel.sendIOTCommand(command, deviceInfo.apikey)
        } else {
            // 发送MD指令
            bleViewModel.sendMDCommand(command)
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
} 