package com.shmedo.mcloudapp.communication.manager

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.mcloudapp.communication.error.DeviceErrorHandler
import com.shmedo.mcloudapp.communication.executor.ResponseDrivenCommandExecutor
import com.shmedo.mcloudapp.communication.model.*
import com.shmedo.mcloudapp.communication.strategy.BleCommunicationStrategy
import com.shmedo.mcloudapp.communication.strategy.CommunicationStrategy
import com.shmedo.mcloudapp.communication.strategy.NetCommunicationStrategy
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.NetIOTCommandViewModel
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * 设备通信管理器
 * 整合通信策略、指令执行器和错误处理器，提供统一的通信接口
 */
class DeviceCommunicationManager(
    private val fragment: Fragment,
    private val deviceInfo: DeviceInfo,
    private val communicateWay: CommunicateWay,
    private val netViewModel: NetIOTCommandViewModel,
    private val bleViewModel: BleViewModel,
    private val bleDevice: DiscoveredBluetoothDevice? = null
) {
    
    // 通信策略
    private val strategy: CommunicationStrategy = createCommunicationStrategy()
    
    // 错误处理器
    private val errorHandler = DeviceErrorHandler(fragment)
    
    // 指令执行器
    private val executor = ResponseDrivenCommandExecutor(
        strategy = strategy,
        errorHandler = errorHandler,
        scope = fragment.lifecycleScope
    )
    
    // 执行状态
    val executionState: StateFlow<CommunicationState> = executor.executionState
    
    /**
     * 创建通信策略
     */
    private fun createCommunicationStrategy(): CommunicationStrategy {
        return when (communicateWay) {
            is NetPlatformConnect -> {
                Timber.i("使用4G网络通信策略")
                NetCommunicationStrategy(netViewModel, deviceInfo)
            }
            is BleConnect -> {
                Timber.i("使用蓝牙通信策略")
                BleCommunicationStrategy(bleViewModel, deviceInfo)
            }
            else -> {
                Timber.w("未知通信方式，默认使用4G网络通信策略")
                NetCommunicationStrategy(netViewModel, deviceInfo)
            }
        }
    }
    
    /**
     * 执行指令序列
     * @param commands 指令列表
     * @param config 执行配置
     * @param onComplete 完成回调
     * @param onError 错误回调
     */
    fun executeCommandSequence(
        commands: List<String>,
        config: CommandSequenceConfig = CommandSequenceConfig(),
        onComplete: (List<CommandResult>) -> Unit = {},
        onError: (DeviceError, String) -> Unit = { _, _ -> }
    ) {
        Timber.i("准备执行指令序列: ${commands.size}条指令，通信方式: ${strategy.getStrategyType()}")
        
        // 显示加载对话框
        if (config.showLoadingDialog) {
            fragment.showLoadingDialog(config.loadingMessage)
        }
        
        // 执行指令序列
        executor.executeSequence(
            commands = commands,
            config = config,
            onComplete = { results ->
                // 隐藏加载对话框
                if (config.showLoadingDialog && config.errorHandling.shouldDismissLoading) {
                    fragment.dismissLoadingDialog()
                }
                onComplete(results)
            },
            onError = { error, command ->
                // 隐藏加载对话框
                if (config.showLoadingDialog && config.errorHandling.shouldDismissLoading) {
                    fragment.dismissLoadingDialog()
                }
                onError(error, command)
            }
        )
    }
    
    /**
     * 发送单条指令 (便捷方法)
     * @param command 指令内容
     * @param config 执行配置
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    fun sendSingleCommand(
        command: String,
        config: CommandSequenceConfig = CommandSequenceConfig(),
        onSuccess: (String) -> Unit = {},
        onError: (DeviceError, String) -> Unit = { _, _ -> }
    ) {
        executeCommandSequence(
            commands = listOf(command),
            config = config,
            onComplete = { results ->
                val firstResult = results.firstOrNull()
                when (firstResult) {
                    is CommandResult.Success -> onSuccess(firstResult.data)
                    is CommandResult.Error -> onError(firstResult.error, firstResult.command)
                    is CommandResult.Timeout -> onError(
                        DeviceError.Timeout(firstResult.command, firstResult.timeoutMs),
                        firstResult.command
                    )
                    null -> onError(DeviceError.Unknown("未收到响应结果"), command)
                }
            },
            onError = onError
        )
    }
    
    /**
     * 处理指令响应 (用于子类重写)
     * 这个方法应该在具体的Fragment中重写，用于处理特定的指令响应
     */
    open fun handleCommandResponse(cmdStr: String) {
        Timber.d("收到指令响应: $cmdStr")
        // 默认实现为空，由子类根据需要重写
    }
    
    /**
     * 处理指令错误 (用于子类重写)
     */
    open fun handleCommandError(error: DeviceError, command: String) {
        Timber.e("指令执行错误: $command, 错误: ${error.message}")
        // 默认实现为空，由子类根据需要重写
    }
    
    /**
     * 处理指令超时 (用于子类重写)
     */
    open fun handleCommandTimeout(command: String, timeoutMs: Long) {
        Timber.e("指令执行超时: $command, 超时时长: ${timeoutMs}ms")
        // 默认实现为空，由子类根据需要重写
    }
    
    /**
     * 取消当前执行
     */
    fun cancelExecution() {
        executor.cancelExecution()
        fragment.dismissLoadingDialog()
    }
    
    /**
     * 检查设备是否已连接
     */
    fun isConnected(): Boolean = strategy.isConnected()
    
    /**
     * 获取连接状态流
     */
    fun getConnectionState() = strategy.getConnectionState()
    
    /**
     * 获取通信策略类型
     */
    fun getStrategyType(): String = strategy.getStrategyType()
    
    /**
     * 检查是否正在执行指令
     */
    fun isExecuting(): Boolean = executor.isExecuting()
    
    /**
     * 清理资源
     */
    fun cleanup() {
        executor.cleanup()
        strategy.cleanup()
    }
} 