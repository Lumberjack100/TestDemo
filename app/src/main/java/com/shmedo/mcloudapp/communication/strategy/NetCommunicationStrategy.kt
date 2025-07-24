package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.core.model.DeviceInfo
import com.shmedo.mcloudapp.communication.model.*
import com.shmedo.mcloudapp.model.*
import com.shmedo.mcloudapp.ui.viewmodel.request.NetIOTCommandViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

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
            
            // 发送指令
            netViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
            
            // 收集响应结果
            netViewModel.cmdDispatchFlow.collect { response ->
                when (response) {
                    is DispatchFailed -> {
                        Timber.e("4G指令派发失败: ${response.errorMsg}")
                        emit(CommandResult.Error(
                            error = DeviceError.Network(response.errorMsg),
                            command = command
                        ))
                        return@collect
                    }
                    
                    is DispatchSuccess -> {
                        Timber.d("4G指令派发成功: $command")
                        // 派发成功后，开始处理结果
                        netViewModel.processCmdResult(command)
                    }
                    
                    is CmdResponseResultError -> {
                        Timber.e("4G响应错误: ${response.errorMsg}")
                        emit(CommandResult.Error(
                            error = DeviceError.Network(response.errorMsg),
                            command = command
                        ))
                        return@collect
                    }
                    
                    is CmdResponseResultTimeOut -> {
                        Timber.e("4G响应超时: ${response.errorMsg}")
                        emit(CommandResult.Timeout(
                            command = command,
                            timeoutMs = config.timeout
                        ))
                        return@collect
                    }
                    
                    is CmdResponseResultSuccess -> {
                        Timber.i("4G响应成功: ${response.cmdResult.responseContent}")
                        emit(CommandResult.Success(
                            data = response.cmdResult.responseContent,
                            command = command
                        ))
                        return@collect
                    }
                    
                    else -> {
                        // 忽略其他类型
                    }
                }
            }
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
                    emit(CommandResult.Error(
                        error = DeviceError.Network(e.message ?: "未知网络错误"),
                        command = command
                    ))
                }
            }
        }

    override fun isConnected(): Boolean {
        return deviceInfo.onlineStatus
    }

    override fun getConnectionState(): Flow<DeviceConnectionState> = flow {
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