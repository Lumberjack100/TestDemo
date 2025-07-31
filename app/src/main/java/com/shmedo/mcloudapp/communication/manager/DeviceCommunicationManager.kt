package com.shmedo.mcloudapp.communication.manager

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shmedo.core.model.DeviceInfo
import com.shmedo.mcloudapp.communication.error.DeviceErrorHandler
import com.shmedo.mcloudapp.communication.executor.ResponseDrivenCommandExecutor
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
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
import timber.log.Timber

/**
 * 设备通信管理器
 * 整合通信策略、指令执行器和错误处理器，提供统一的通信接口
 * 支持实时回调，每条指令处理完就回调
 */
class DeviceCommunicationManager(
    private val fragment: Fragment,
    private val deviceInfo: DeviceInfo,
    private val communicateWay: CommunicateWay,
    private val netViewModel: NetIOTCommandViewModel,
    private val bleViewModel: BleViewModel,
) {
    // 通信策略
    private val strategy: CommunicationStrategy = createCommunicationStrategy()

    // 错误处理器
    private val errorHandler = DeviceErrorHandler(fragment)

    // 指令执行器
    private val executor = ResponseDrivenCommandExecutor(
        strategy = strategy,
        errorHandler = errorHandler,
        scope = fragment.viewLifecycleOwner.lifecycleScope
    )

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
     * 执行指令序列 (支持实时回调)
     * @param commands 指令列表
     * @param config 执行配置
     * @param callbacks 回调配置
     */
    fun executeCommandSequence(
        commands: List<String>,
        config: CommandSequenceConfig = CommandSequenceConfig(),
        callbacks: CommandSequenceCallbacks = CommandSequenceCallbacks()
    ) {
        Timber.i("准备执行指令序列: 共 ${commands.size} 条指令，通信方式: ${strategy.getStrategyType()}")

        // 显示加载对话框
        if (config.showLoadingDialog) {
            fragment.showLoadingDialog(config.loadingMessage)
        }

        // 执行指令序列
        executor.executeSequence(
            commands = commands,
            config = config,
            callbacks = CommandSequenceCallbacks(
                onSuccess = { successResult ->
                    // 实时回调处理
                    callbacks.onSuccess?.invoke(successResult)
                },
                onComplete = { results ->
                    // 隐藏加载对话框
                    if (config.showLoadingDialog && config.errorConfig.shouldDismissLoading) {
                        fragment.dismissLoadingDialog()
                    }
                    callbacks.onComplete(results)
                },
                onError = { error, command ->
                    // 隐藏加载对话框
                    if (config.showLoadingDialog && config.errorConfig.shouldDismissLoading) {
                        fragment.dismissLoadingDialog()
                    }
                    callbacks.onError(error, command)
                }
            )
        )
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