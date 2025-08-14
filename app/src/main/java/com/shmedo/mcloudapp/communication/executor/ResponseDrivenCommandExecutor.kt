package com.shmedo.mcloudapp.communication.executor

import com.shmedo.mcloudapp.communication.error.DeviceErrorHandler
import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.strategy.CommunicationStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 响应驱动指令执行器
 * 严格按照"发送→等待响应→发送下一条"的业务逻辑执行指令序列
 * 支持实时回调，每条指令处理完就回调
 */
class ResponseDrivenCommandExecutor(
    private val strategy: CommunicationStrategy,
    private val errorHandler: DeviceErrorHandler,
    private val scope: CoroutineScope
) {

    // 执行锁，确保同时只有一个指令序列在执行
    private val isExecuting = AtomicBoolean(false)

    // 当前执行的Job
    private var currentExecutionJob: Job? = null

    // 执行结果收集
    private val executionResults = mutableListOf<CommandResult>()

    /**
     * 执行指令序列 (支持实时回调)
     * @param commands 指令列表
     * @param config 执行配置
     * @param callbacks 回调配置
     */
    fun executeSequence(
        commands: List<String>,
        config: CommandSequenceConfig,
        callbacks: CommandSequenceCallbacks = CommandSequenceCallbacks()
    ) {
        if (commands.isEmpty()) {
            Timber.w("指令列表为空，跳过执行")
            callbacks.onComplete(emptyList())
            return
        }

        // 检查是否已有指令在执行
        if (!isExecuting.compareAndSet(false, true)) {
            val error = DeviceError.Validation("已有指令序列在执行中，请等待完成后再试")
            errorHandler.handleError(error, config.errorConfig)
            callbacks.onError(error, "")
            return
        }

        Timber.i("开始执行指令序列，共 ${commands.size} 条指令")

        currentExecutionJob = scope.launch {
            try {
                executeSequenceInternal(commands, config, callbacks)
            } finally {
                isExecuting.set(false)
            }
        }
    }

    /**
     * 内部执行逻辑 (支持实时回调)
     */
    private suspend fun executeSequenceInternal(
        commands: List<String>,
        config: CommandSequenceConfig,
        callbacks: CommandSequenceCallbacks
    ) {
        executionResults.clear()

        try {
            // 逐个执行指令
            for ((index, command) in commands.withIndex()) {
                // 检查连接状态
                if (!strategy.isConnected()) {
                    val error = DeviceError.Disconnected(strategy.getStrategyType())
                    handleExecutionError(error, command, config, callbacks)
                    return
                }

                Timber.i("执行第 ${index + 1}/${commands.size} 条指令: $command")

                // 执行单条指令
                val result = executeSingleCommand(command, config)
                executionResults.add(result)

                // 处理结果
                when (result) {
                    is CommandResult.Success -> {
                        // 检查是否是最后一条指令
                        if (index == commands.size - 1) {
                            Timber.i("指令序列执行完成，成功${getSuccessCount()}条，失败${getErrorCount()}条")
                            isExecuting.set(false)
                            callbacks.onComplete.invoke(executionResults.toList())
                        }

                        // 检查是否启用了业务层解析失败中断功能
                        if (config.enableBusinessParseFailureInterrupt) {
                            // 实时回调处理，检查是否应该继续执行
                            val shouldContinue = callbacks.onSuccess?.invoke(result) ?: true

                            if (!shouldContinue) {
                                Timber.i("业务层要求中断指令序列执行")
                                isExecuting.set(false)
                                callbacks.onComplete(executionResults.toList())
                                return
                            }
                        } else {
                            // 保持原有逻辑：只是回调，不检查返回值
                            callbacks.onSuccess?.invoke(result)
                        }

                        //最后一条指令,返回
                        if (index == commands.size - 1) return

                        // 继续执行下一条指令
                    }

                    is CommandResult.Error -> {
                        Timber.e("指令执行失败: $command, 错误: ${result.error.message}")
                        if (config.stopOnFirstCmdError) {
                            handleExecutionError(result.error, command, config, callbacks)
                            return
                        }

                        callbacks.onError(result.error, command)
                        // 记录错误但继续执行
                        Timber.w("忽略错误，继续执行下一条指令")
                    }

                    is CommandResult.Timeout -> {
                        Timber.e("指令超时: $command")
                        val timeoutError = DeviceError.Timeout(command, result.timeoutMs)
                        if (config.stopOnFirstCmdError) {
                            handleExecutionError(timeoutError, command, config, callbacks)
                            return
                        }

                        callbacks.onError(timeoutError, command)
                        Timber.w("忽略超时，继续执行下一条指令")
                    }
                }
            }

            // 所有指令执行完成
            Timber.i("指令序列执行完成，成功${getSuccessCount()}条，失败${getErrorCount()}条")
            isExecuting.set(false)
            callbacks.onComplete(executionResults.toList())

        } catch (e: Exception) {
            Timber.e(e, "指令序列执行异常")
            val error = DeviceError.Unknown("指令序列执行异常 ${e.message ?: ""}", e)
            handleExecutionError(error, "", config, callbacks)
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
                error = DeviceError.Unknown("执行指令异常 ${e.message ?: ""}", e),
                command = command
            )
        }
    }

    /**
     * 处理执行错误 (支持实时回调)
     */
    private fun handleExecutionError(
        error: DeviceError,
        command: String,
        config: CommandSequenceConfig,
        callbacks: CommandSequenceCallbacks,
    ) {
        isExecuting.set(false)
        errorHandler.handleError(error, config.errorConfig)
        callbacks.onError(error, command)
    }

    /**
     * 取消当前执行
     */
    fun cancelExecution() {
        currentExecutionJob?.cancel()
        isExecuting.set(false)
        Timber.i("清理指令序列执行 Job")
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
        executionResults.clear()
    }
}