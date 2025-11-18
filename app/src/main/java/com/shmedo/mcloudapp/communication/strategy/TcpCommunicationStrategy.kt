package com.shmedo.mcloudapp.communication.strategy

import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.tcp.TcpSuccessDataResult
import com.shmedo.mcloudapp.communication.model.CommandConfig
import com.shmedo.mcloudapp.communication.model.CommandResult
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.ui.viewmodel.request.TcpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import timber.log.Timber

/**
 * TCP通信策略实现（方案2：简化顺序处理）
 *
 * 设计思路：
 * - 利用ResponseDrivenCommandExecutor的串行执行特性
 * - 维护单个响应Channel，顺序处理请求-响应
 * - 为将来升级到会话管理机制预留扩展接口
 *
 * 适用场景：
 * - 单一页面通信，指令响应一对一
 * - 不需要支持并发请求
 *
 * @author gonghe
 * @date 2025/11/17
 */
class TcpCommunicationStrategy(
    private val tcpViewModel: TcpViewModel,
    private val deviceInfo: DeviceInfo,
    private val scope: CoroutineScope
) : CommunicationStrategy {

    // 当前等待响应的Channel（简化方案2）
    @Volatile
    private var currentResponseChannel: Channel<String>? = null

    init {
        // 启动TCP数据监听
        startTcpDataListener()
    }

    /**
     * 启动TCP数据监听
     * 监听TCP响应数据并分发到当前等待的Channel
     */
    private fun startTcpDataListener() {
        tcpViewModel.data.onEach { result ->
            when (result) {
                is TcpSuccessDataResult -> {
                    val response = result.data
                    Timber.d("TCP收到响应: $response")

                    // 将响应发送给当前等待的Channel
                    currentResponseChannel?.trySend(response)
                }

                else -> {
                    // 其他类型的结果（连接状态变化等）由getConnectionState处理
                }
            }
        }.launchIn(scope)
    }

    override fun sendCommand(command: String, config: CommandConfig): Flow<CommandResult> {
        val sessionId = "tcp_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
        val responseChannel = Channel<String>(1)

        return flow {
            // 检查TCP连接状态
            if (!isConnected()) {
                emit(CommandResult.Error(DeviceError.Disconnected("TCP"), command))
                return@flow
            }

            Timber.i("TCP发送指令 [Session: $sessionId]: $command")

            // 延迟发送（如果配置了延迟）
            if (config.delayBeforeSend > 0) {
                delay(config.delayBeforeSend)
            }

            // 设置当前响应Channel
            currentResponseChannel = responseChannel

            // 发送指令
            tcpViewModel.sendMsgToServer(command)

            // 等待响应（带超时）
            withTimeout(config.timeout) {
                val response = responseChannel.receive()
                Timber.i("TCP响应内容 [Session: $sessionId]: $response")
                emit(CommandResult.Success(response, command))
            }

        }.onCompletion { cause ->
            // 清理资源
            Timber.d("流程结束清理TCP会话资源 [Session: $sessionId], 原因: $cause")
            currentResponseChannel = null
            responseChannel.close()

        }.catch { e ->
            when (e) {
                is TimeoutCancellationException -> {
                    emit(CommandResult.Timeout(command, config.timeout))
                }

                else -> {
                    Timber.e(e, "TCP通信异常")
                    emit(
                        CommandResult.Error(
                            error = DeviceError.Tcp(e.message ?: "未知TCP错误"),
                            command = command
                        )
                    )
                }
            }
        }
    }

    override fun isConnected(): Boolean {
        return tcpViewModel.isConnected()
    }

    override fun getConnectionState(): Flow<DeviceConnectionState> {
        return tcpViewModel.connectionState
    }

    override fun getStrategyType(): String = "TCP"

    override fun cleanup() {
        // 清理TCP相关资源
        currentResponseChannel?.close()
        currentResponseChannel = null
        Timber.d("TCP通信策略资源已清理")
    }

    override fun addDeviceLogItem(priority: Int, data: String) {
        tcpViewModel.addLogItem(
            sessionId = CommonMMKVOwner.iotDeviceLogSessionId,
            priority = priority,
            data = data
        )
    }

    // ==================== 会话管理扩展接口（预留） ====================
    // 当前使用简化方案2：单Channel顺序处理
    // 未来升级到方案1时，取消注释以下代码，实现精确会话管理

    /*
    private data class TcpCommandSession(
        val sessionId: String,
        val channel: Channel<String>,
        val command: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val sessionMap = ConcurrentHashMap<String, TcpCommandSession>()

    /**
     * 注册会话
     */
    private fun registerSession(sessionId: String, channel: Channel<String>, command: String) {
        sessionMap[sessionId] = TcpCommandSession(sessionId, channel, command)
        Timber.d("注册TCP会话: $sessionId")
    }

    /**
     * 注销会话
     */
    private fun unregisterSession(sessionId: String) {
        sessionMap.remove(sessionId)?.let {
            it.channel.close()
            Timber.d("注销TCP会话: $sessionId")
        }
    }

    /**
     * 分发响应到对应的会话
     */
    private fun distributeResponse(response: String) {
        // 解析响应中的msgid
        val msgId = extractMsgId(response)
        
        if (msgId != null) {
            // 根据msgid找到对应的session
            val session = sessionMap.values.find { 
                extractMsgId(it.command) == msgId 
            }
            
            if (session != null) {
                session.channel.trySend(response)
                Timber.d("响应已分发到会话: ${session.sessionId}")
            } else {
                Timber.w("未找到匹配的会话，msgid: $msgId")
            }
        } else {
            Timber.w("响应中未找到msgid，无法分发")
        }
    }

    /**
     * 从指令或响应中提取msgid
     */
    private fun extractMsgId(data: String): String? {
        return Regex("msgid=([^&\\s]+)").find(data)?.groupValues?.get(1)
    }

    /**
     * 清理超时的会话
     */
    private fun cleanupTimeoutSessions(timeoutMs: Long = 30000) {
        val now = System.currentTimeMillis()
        val timeoutSessions = sessionMap.values.filter { 
            now - it.timestamp > timeoutMs 
        }
        
        timeoutSessions.forEach { session ->
            Timber.w("清理超时会话: ${session.sessionId}")
            unregisterSession(session.sessionId)
        }
    }
    */
}

