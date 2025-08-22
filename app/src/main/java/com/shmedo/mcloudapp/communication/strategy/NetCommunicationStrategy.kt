package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.core.model.DeviceInfo
import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.ui.viewmodel.request.CommandResponse
import com.shmedo.mcloudapp.ui.viewmodel.request.NetIOTCommandViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * 4G网络通信策略实现
 * 集成现有的NetIOTCommandViewModel进行网络通信
 */
class NetCommunicationStrategy(
    private val netViewModel: NetIOTCommandViewModel,
    private val deviceInfo: DeviceInfo
) : CommunicationStrategy {

    override suspend fun sendCommand(command: String, config: CommandConfig): Flow<CommandResult> =
        flow {
            Timber.d("4G发送指令: $command")

            // 延迟发送（如果配置了延迟）
            if (config.delayBeforeSend > 0) {
                delay(config.delayBeforeSend)
            }

            // 使用新的简洁API发送指令
            val response = netViewModel.sendCommandAndAwaitResponse(
                command = command,
                deviceTokens = listOf(deviceInfo.deviceToken),
                timeoutMs = config.timeout
            )

            // 转换为通用的 CommandResult
            val result = when (response) {
                is CommandResponse.Success -> {
                    CommandResult.Success(
                        responseData = response.responseData,
                        command = command,
                        timestamp = response.timestamp
                    )
                }

                is CommandResponse.Error -> {
                    CommandResult.Error(
                        error = DeviceError.Network(response.errorMsg),
                        command = command
                    )
                }

                is CommandResponse.Timeout -> {
                    CommandResult.Timeout(
                        command = command,
                        timeoutMs = response.timeoutMs
                    )
                }
            }

            emit(result)

        }.catch { e ->
            // 使用Flow.catch处理异常，避免Flow异常透明度违规
            when (e) {
                is CancellationException -> {
                    // Flow被取消，不发出任何值，避免异常透明度违规
                    Timber.d("4G指令被取消: $command")
                    // 不emit任何值，让Flow自然结束
                }

                else -> {
                    // 其他异常
                    Timber.e(e, "4G通信异常")
                    emit(
                        CommandResult.Error(
                            error = DeviceError.Network(e.message ?: "未知网络错误"),
                            command = command
                        )
                    )
                }
            }
        }


    override fun isConnected(): Boolean {
        return deviceInfo.onlineStatus
    }

    override fun getConnectionState(): Flow<DeviceConnectionState> = flow {
        // 可以根据需要动态检查设备状态
        if (isConnected()) {
            emit(DeviceConnectionState.Connected)
        } else {
            emit(DeviceConnectionState.Disconnected)
        }
    }

    override fun getStrategyType(): String = "4G网络"

    override fun cleanup() {
        // 清理网络相关资源
        netViewModel.clearCommandQueue()
    }
} 