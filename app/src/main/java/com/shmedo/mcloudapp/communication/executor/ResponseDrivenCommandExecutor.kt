package com.shmedo.mcloudapp.communication.executor

import com.shmedo.mcloudapp.communication.error.DeviceErrorHandler
import com.shmedo.mcloudapp.communication.model.*
import com.shmedo.mcloudapp.communication.strategy.CommunicationStrategy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 响应驱动指令执行器
 * 严格按照"发送→等待响应→发送下一条"的业务逻辑执行指令序列
 */
class ResponseDrivenCommandExecutor(
    private val strategy: CommunicationStrategy,
    private val errorHandler: DeviceErrorHandler,
    private val scope: CoroutineScope
) {
    
    // 指令队列
    private val commandQueue = LinkedList<CommandItem>()
    
    // 执行状态
    private val _executionState = MutableStateFlow<CommunicationState>(CommunicationState.Idle)
    val executionState: StateFlow<CommunicationState> = _executionState.asStateFlow()
    
    // 执行锁，确保同时只有一个指令序列在执行
    private val isExecuting = AtomicBoolean(false)
    
    // 当前执行的Job
    private var currentExecutionJob: Job? = null
    
    // 执行结果收集
    private val executionResults = mutableListOf<CommandResult>()

    /**
     * 执行指令序列
     * @param commands 指令列表
     * @param config 执行配置
     * @param onComplete 完成回调
     * @param onError 错误回调
     */
    fun executeSequence(
        commands: List<String>,
        config: CommandSequenceConfig,
        onComplete: (List<CommandResult>) -> Unit = {},
        onError: (DeviceError, String) -> Unit = { _, _ -> }
    ) {
        if (commands.isEmpty()) {
            Timber.w("指令列表为空，跳过执行")
            onComplete(emptyList())
            return
        }
        
        // 检查是否已有指令在执行
        if (!isExecuting.compareAndSet(false, true)) {
            val error = DeviceError.Validation("已有指令序列在执行中，请等待完成后再试")
            errorHandler.handleError(error, config.errorHandling)
            onError(error, "")
            return
        }
        
        Timber.i("开始执行指令序列，共${commands.size}条指令")
        
        currentExecutionJob = scope.launch {
            try {
                executeSequenceInternal(commands, config, onComplete, onError)
            } finally {
                isExecuting.set(false)
            }
        }
    }
    
    /**
     * 内部执行逻辑
     */
    private suspend fun executeSequenceInternal(
        commands: List<String>,
        config: CommandSequenceConfig,
        onComplete: (List<CommandResult>) -> Unit,
        onError: (DeviceError, String) -> Unit
    ) {
        executionResults.clear()
        
        try {
            // 设置执行状态
            _executionState.value = CommunicationState.Executing(
                currentCommand = commands.first(),
                remainingCount = commands.size
            )
            
            // 逐个执行指令
            for ((index, command) in commands.withIndex()) {
                // 检查连接状态
                if (!strategy.isConnected()) {
                    val error = DeviceError.Disconnected(strategy.getStrategyType())
                    handleExecutionError(error, command, config, onError)
                    return
                }
                
                Timber.d("执行第${index + 1}/${commands.size}条指令: $command")
                
                // 更新执行状态
                _executionState.value = CommunicationState.Executing(
                    currentCommand = command,
                    remainingCount = commands.size - index
                )
                
                // 执行单条指令
                val result = executeSingleCommand(command, config)
                executionResults.add(result)
                
                // 处理结果
                when (result) {
                    is CommandResult.Success -> {
                        Timber.d("指令执行成功: $command")
                        // 继续执行下一条指令
                    }
                    
                    is CommandResult.Error -> {
                        Timber.e("指令执行失败: $command, 错误: ${result.error.message}")
                        if (config.stopOnFirstError) {
                            handleExecutionError(result.error, command, config, onError)
                            return
                        } else {
                            // 记录错误但继续执行
                            Timber.w("忽略错误，继续执行下一条指令")
                        }
                    }
                    
                    is CommandResult.Timeout -> {
                        Timber.e("指令超时: $command")
                        val timeoutError = DeviceError.Timeout(command, result.timeoutMs)
                        if (config.stopOnFirstError) {
                            handleExecutionError(timeoutError, command, config, onError)
                            return
                        } else {
                            Timber.w("忽略超时，继续执行下一条指令")
                        }
                    }
                }
            }
            
            // 所有指令执行完成
            _executionState.value = CommunicationState.Completed(executionResults.toList())
            Timber.i("指令序列执行完成，成功${getSuccessCount()}条，失败${getErrorCount()}条")
            onComplete(executionResults.toList())
            
        } catch (e: CancellationException) {
            Timber.d("指令序列执行被取消")
            _executionState.value = CommunicationState.Idle
        } catch (e: Exception) {
            Timber.e(e, "指令序列执行异常")
            val error = DeviceError.Unknown(e.message ?: "未知错误", e)
            handleExecutionError(error, "", config, onError)
        }
    }
    
    /**
     * 执行单条指令
     */
    private suspend fun executeSingleCommand(
        command: String,
        config: CommandSequenceConfig
    ): CommandResult {
        return try {
            val commandConfig = CommandConfig(
                timeout = config.timeout,
                delayBeforeSend = config.delayBeforeSend
            )
            
            // 发送指令并等待响应
            val resultFlow = strategy.sendCommand(command, commandConfig)
            
            // 收集第一个结果
            resultFlow.first()
            
        } catch (e: Exception) {
            Timber.e(e, "执行指令异常: $command")
            CommandResult.Error(
                error = DeviceError.Unknown(e.message ?: "指令执行异常", e),
                command = command
            )
        }
    }
    
    /**
     * 处理执行错误
     */
    private fun handleExecutionError(
        error: DeviceError,
        command: String,
        config: CommandSequenceConfig,
        onError: (DeviceError, String) -> Unit
    ) {
        _executionState.value = CommunicationState.Failed(error, executionResults.toList())
        errorHandler.handleError(error, config.errorHandling)
        onError(error, command)
    }
    
    /**
     * 取消当前执行
     */
    fun cancelExecution() {
        currentExecutionJob?.cancel()
        _executionState.value = CommunicationState.Idle
        isExecuting.set(false)
        Timber.i("指令序列执行已取消")
    }
    
    /**
     * 检查是否正在执行
     */
    fun isExecuting(): Boolean = isExecuting.get()
    
    /**
     * 获取成功执行的指令数量
     */
    private fun getSuccessCount(): Int = executionResults.count { it is CommandResult.Success }
    
    /**
     * 获取执行失败的指令数量
     */
    private fun getErrorCount(): Int = executionResults.count { it !is CommandResult.Success }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        cancelExecution()
        commandQueue.clear()
        executionResults.clear()
    }
} 