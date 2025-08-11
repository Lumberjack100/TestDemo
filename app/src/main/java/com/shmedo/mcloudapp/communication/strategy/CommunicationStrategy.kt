package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import kotlinx.coroutines.flow.Flow

/**
 * 通信策略接口
 * 用于抽象4G网络和蓝牙通信的差异
 */
interface CommunicationStrategy {
    
    /**
     * 发送指令
     * @param command 指令内容
     * @param config 指令配置
     * @return 指令执行结果Flow
     */
    suspend fun sendCommand(command: String, config: CommandConfig = CommandConfig()): Flow<CommandResult>
    
    /**
     * 检查设备是否已连接
     */
    fun isConnected(): Boolean
    
    /**
     * 获取连接状态流
     */
    fun getConnectionState(): Flow<DeviceConnectionState>
    
    /**
     * 获取策略类型标识
     */
    fun getStrategyType(): String
    
    /**
     * 清理资源
     */
    fun cleanup()
} 