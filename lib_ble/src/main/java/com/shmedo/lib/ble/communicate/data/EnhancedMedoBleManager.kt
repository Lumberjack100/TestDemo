/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.shmedo.lib.ble.communicate.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.shmedo.lib.ble.communicate.parser.CommandResponse
import com.shmedo.lib.ble.communicate.parser.PacketMerger
import com.shmedo.lib.ble.communicate.spec.BleDeviceSpecManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

/**
 * 增强版指令发送配置
 */
data class EnhancedSendConfig(
    val responseTimeoutMs: Long = 15000,        // 等待指令响应超时时间（毫秒）
)

/**
 * 会话优先级枚举
 * 数值越小优先级越高
 */
enum class SessionPriority(val value: Int) {
    /** 紧急指令（如重启、急停） */
    URGENT(0),
    /** 高优先级（如实时数据查询） */
    HIGH(1),
    /** 普通指令 */
    NORMAL(2),
    /** 低优先级（如批量数据） */
    LOW(3)
}

/**
 * 会话信息
 */
data class BleCommandSession(
    val sessionId: String,
    val ownerId: String,
    val priority: SessionPriority = SessionPriority.NORMAL,
    val createdTime: Long = System.currentTimeMillis()
)

/**
 * 带会话的指令
 */
data class SessionCommand(
    val command: String,
    val session: BleCommandSession,
    val timestamp: Long = System.currentTimeMillis(),
    val timeoutMs: Long = 10000,
    val retryCount: Int = 0
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > timeoutMs
    }
}

/**
 * 会话响应路由器（简化版本，避免循环依赖）
 * 负责将蓝牙响应路由到正确的会话监听器
 */
class BleSessionResponseRouter {
    // 使用ConcurrentHashMap提高并发性能
    private val sessionListeners = ConcurrentHashMap<String, (CommandData) -> Unit>()
    private val mutex = Mutex()

    /**
     * 注册会话监听器
     */
    suspend fun registerSession(sessionId: String, listener: (CommandData) -> Unit) = mutex.withLock {
        sessionListeners[sessionId] = listener
        Timber.v("BLE注册会话监听器: $sessionId, 当前活跃会话数: ${sessionListeners.size}")
    }

    /**
     * 注销会话监听器
     */
    suspend fun unregisterSession(sessionId: String) = mutex.withLock {
        sessionListeners.remove(sessionId)
        Timber.d("BLE注销会话监听器: $sessionId, 剩余活跃会话数: ${sessionListeners.size}")
    }

    /**
     * 将响应路由到指定会话
     */
    suspend fun routeToSession(sessionId: String, commandData: CommandData) {
        val listener = sessionListeners[sessionId]
        if (listener != null) {
            try {
                listener(commandData)
                Timber.v("BLE响应已路由到会话: $sessionId")
            } catch (e: Exception) {
                Timber.e(e, "BLE会话监听器执行异常: $sessionId")
            }
        } else {
            Timber.w("BLE未找到会话监听器: $sessionId")
        }
    }

    /**
     * 获取活跃会话数量
     */
    fun getActiveSessionCount(): Int = sessionListeners.size

    /**
     * 清理所有会话
     */
    suspend fun clearAllSessions() = mutex.withLock {
        sessionListeners.clear()
    }
}

/**
 * 增强版 MedoBleManager，支持会话管理和优先级队列
 */
class EnhancedMedoBleManager(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
    private val sendConfig: EnhancedSendConfig = EnhancedSendConfig()
) : BleManager(context) {
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    // 设备规格管理器
    private val deviceSpecManager = BleDeviceSpecManager()

    // 使用优先级队列替代普通队列
    private val commandQueue = PriorityBlockingQueue<SessionCommand>(
        100, // 初始容量
        compareBy<SessionCommand> { it.session.priority.value }
            .thenBy { it.timestamp } // 同优先级按时间排序
    )

    // 会话响应路由器
    private val sessionResponseRouter = BleSessionResponseRouter()

    // 活跃会话管理
    private val activeSessions = ConcurrentHashMap<String, BleCommandSession>()

    @Volatile
    private var isProcessingQueue = false
    private val queueMutex = Mutex()

    @Volatile
    private var lastResponseTime = System.currentTimeMillis()

    @Volatile
    private var currentSessionCommand: SessionCommand? = null

    // 使用 stateAsFlow() 获取连接状态
    val connectionState = stateAsFlow()

    // 兼容原有接口 - 全局数据响应流
    private val _commandData = MutableSharedFlow<CommandData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val commandData = _commandData.asSharedFlow()

    override fun log(priority: Int, message: String) {
        // logger.log(priority, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        // Increase the MTU
        requestMtu(512)
            .fail { device, status ->
                Timber.e("requestMtu error: ${device.name} $status")
            }
            .enqueue()

        // Enable notifications
        setNotificationCallback(notifyCharacteristic)
            // Merges packets until the entire text is present in the stream [PacketMerger.merge].
            .merge(PacketMerger(enableDetailedLogging = true))
            .asValidResponseFlow<CommandResponse>()
            .onEach { commandResponse ->
                try {
                    processCommandResponse(commandResponse)
                } catch (e: Exception) {
                    Timber.e(e, "处理指令响应时出错")
                }
            }
            .launchIn(scope)

        enableNotifications(notifyCharacteristic)
            .fail { device, status ->
                Timber.e("enableNotifications error: ${device.name} $status")
            }
            .enqueue()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val discoveryResult = deviceSpecManager.identifyDevice(gatt)

        return if (discoveryResult != null) {
            // 保存发现的设备信息
            notifyCharacteristic = discoveryResult.notifyCharacteristic
            writeCharacteristic = discoveryResult.writeCharacteristic

            Timber.d("设备蓝牙芯片识别成功: ${discoveryResult.spec.deviceName}")
            true
        } else {
            Timber.w("未找到支持的设备规格")
            false
        }
    }

    override fun onServicesInvalidated() {
        writeCharacteristic = null
        notifyCharacteristic = null
    }

    /**
     * 发送带会话的指令
     */
    fun sendSessionCommand(sessionCommand: SessionCommand) {
        if (sessionCommand.command.isBlank()) {
            Timber.w("尝试发送空指令，已忽略")
            return
        }

        // 注册会话
        activeSessions[sessionCommand.session.sessionId] = sessionCommand.session
        
        commandQueue.offer(sessionCommand)
        
        Timber.v("会话指令已加入队列: ${sessionCommand.command}, 会话Id: ${sessionCommand.session.sessionId}, 会话优先级: ${sessionCommand.session.priority}, 当前队列大小: ${commandQueue.size}")

        // 如果当前没有正在处理队列，则开始处理
        if (!isProcessingQueue) {
            scope.launch {
                processCommandQueue()
            }
        }
    }

    /**
     * 兼容原有接口：发送普通指令
     */
    fun sendData(command: String) {
        if (command.isBlank()) {
            Timber.w("尝试发送空指令，已忽略")
            return
        }

        // 创建默认会话
        val defaultSession = BleCommandSession(
            sessionId = "default_${System.currentTimeMillis()}",
            ownerId = "legacy",
            priority = SessionPriority.NORMAL
        )

        val sessionCommand = SessionCommand(
            command = command,
            session = defaultSession
        )

        sendSessionCommand(sessionCommand)
    }

    /**
     * 注册会话监听器
     */
    suspend fun registerSessionListener(sessionId: String, listener: (CommandData) -> Unit) {
        sessionResponseRouter.registerSession(sessionId, listener)
    }

    /**
     * 注销会话监听器
     */
    suspend fun unregisterSessionListener(sessionId: String) {
        sessionResponseRouter.unregisterSession(sessionId)
    }

    /**
     * 取消指定会话的所有指令
     */
    fun cancelSession(sessionId: String) {
        commandQueue.removeAll { it.session.sessionId == sessionId }
        activeSessions.remove(sessionId)
        scope.launch {
            sessionResponseRouter.unregisterSession(sessionId)
        }
        Timber.i("会话 $sessionId 的所有指令已取消")
    }

    /**
     * 清理过期或无效会话
     */
    fun cleanupExpiredSessions() {
        val currentTime = System.currentTimeMillis()
        val expiredSessions = activeSessions.values.filter { 
            currentTime - it.createdTime > SESSION_TIMEOUT_MS 
        }
        
        expiredSessions.forEach { session ->
            cancelSession(session.sessionId)
        }
    }

    /**
     * 处理指令响应
     */
    private suspend fun processCommandResponse(commandResponse: CommandResponse) {
        if (commandResponse.latestResponse.isEmpty()) {
            Timber.w("收到空响应内容")
            return
        }

        val commandData = CommandData(response = commandResponse.latestResponse)

        val currentSession = currentSessionCommand
        if (currentSession != null) {
            // 路由到特定会话
            sessionResponseRouter.routeToSession(currentSession.session.sessionId, commandData)
        }

        // 兼容原有接口，同时广播给全局监听器
        _commandData.emit(commandData)

        // 启用了响应驱动的流控，触发下一个指令发送
        lastResponseTime = System.currentTimeMillis()
        processNextCommand()
    }

    /**
     * 处理下一个指令（由响应触发）
     */
    private fun processNextCommand() {
        if (commandQueue.isNotEmpty() && !isProcessingQueue) {
            scope.launch {
                processCommandQueue()
            }
        }
    }

    /**
     * 处理指令队列
     */
    private suspend fun processCommandQueue() {
        if (isProcessingQueue) {
            return // 已经有其他协程在处理队列
        }

        isProcessingQueue = true

        try {
            while (commandQueue.isNotEmpty()) {
                val sessionCommand = commandQueue.poll() ?: break
                currentSessionCommand = sessionCommand

                // 检查会话是否仍然有效
                if (!activeSessions.containsKey(sessionCommand.session.sessionId)) {
                    Timber.w("会话已失效，跳过指令: ${sessionCommand.command}")
                    continue
                }

                // 检查指令是否超时
                if (sessionCommand.isExpired()) {
                    Timber.w("指令已超时，跳过: ${sessionCommand.command}")
                    notifySessionTimeout(sessionCommand)
                    continue
                }

                sendDataInternal(sessionCommand.command)

                //流控机制
                waitForResponseOrTimeout()
            }
        } finally {
            isProcessingQueue = false
            currentSessionCommand = null
        }
    }

    /**
     * 通知会话超时
     */
    private suspend fun notifySessionTimeout(sessionCommand: SessionCommand) {
        val timeoutData = CommandData(response = "TIMEOUT:${sessionCommand.command}")
        sessionResponseRouter.routeToSession(sessionCommand.session.sessionId, timeoutData)
    }

    /**
     * 内部发送数据方法
     */
    private suspend fun sendDataInternal(command: String): Boolean {
        return queueMutex.withLock {
            try {
                writeCharacteristic?.let { characteristic ->
                    Timber.v("写入特征数据: length=${command.toByteArray().size} bytes;content: $command")

                    // 智能选择写入类型
                    val writeType =
                        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        } else {
                            characteristic.writeType
                        }

                    writeCharacteristic(characteristic, Data.from(command), writeType)
                        .split()
                        .enqueue()

                    true
                } ?: run {
                    Timber.e("writeCharacteristic 为空，无法发送数据")
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "发送数据时出现异常: $command")
                false
            }
        }
    }

    /**
     * 等待响应或超时
     */
    private suspend fun waitForResponseOrTimeout() {
        val startTime = System.currentTimeMillis()
        val initialResponseTime = lastResponseTime

        while (System.currentTimeMillis() - startTime < sendConfig.responseTimeoutMs) {
            if (lastResponseTime > initialResponseTime) {
                // 收到新的响应
//                Timber.v("收到响应，继续处理下一个指令")
                return
            }
            delay(50) // 短暂等待
        }

        Timber.w("等待响应超时")
    }

    /**
     * 清空指令队列
     */
    fun clearCommandQueue() {
        commandQueue.clear()
        activeSessions.clear()
        scope.launch {
            sessionResponseRouter.clearAllSessions()
        }
        Timber.i("指令队列已清空")
    }

    /**
     * 获取队列状态
     */
    fun getQueueStatus(): Pair<Int, Boolean> {
        return commandQueue.size to isProcessingQueue
    }

    /**
     * 获取会话状态
     */
    fun getSessionStatus(): Triple<Int, Int, Int> {
        return Triple(
            activeSessions.size,
            commandQueue.size,
            sessionResponseRouter.getActiveSessionCount()
        )
    }

    suspend fun connect() {
        try {
            connect(device)
                .useAutoConnect(false)
                // Automatic retries are supported, in case of 133 error.
                .retry(3, 300)
                // A connection timeout can be set. This is additional to the Android's connection timeout which is 30 seconds.
                .timeout(10_000)
                // To suspend until the connection AND initialization is complete, call suspend().
                .suspend()
        } catch (e: Exception) {
            // 处理异常
            Timber.e(e, "连接时出现异常")
            // 根据需要决定是否抛出异常或进行其他处理
        }
    }

    fun release() {
        clearCommandQueue()
        cancelQueue()
        disconnect().enqueue()
    }

    companion object {
        private const val SESSION_TIMEOUT_MS = 30_000L // 30秒会话超时
    }
}
