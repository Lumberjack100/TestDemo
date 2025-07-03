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
import com.shmedo.lib.ble.communicate.service.base.BleManagerResult
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.ReadyResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
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
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.observer.ConnectionObserver
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 指令发送配置
 */
data class SendConfig(
    val useResponseBasedFlow: Boolean = true,  // 是否使用基于响应的流控
    val responseTimeoutMs: Long = 5000,        // 响应超时时间（毫秒）
    val fallbackDelayMs: Long = 1000,           // 备用延时（毫秒）
)

/**
 * 待发送的指令
 */
private data class PendingCommand(
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MedoBleManager(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
    private val sendConfig: SendConfig = SendConfig()
) : BleManager(context) {
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    // 设备规格管理器
    private val deviceSpecManager = BleDeviceSpecManager()

    // 指令队列管理 - 使用线程安全的 ConcurrentLinkedQueue
    private val commandQueue = ConcurrentLinkedQueue<PendingCommand>()

    @Volatile
    private var isProcessingQueue = false
    private val queueMutex = Mutex()

    @Volatile
    private var lastResponseTime = System.currentTimeMillis()

    private val _data = MutableSharedFlow<BleManagerResult<CommandData>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val data = _data.asSharedFlow()

    init {
        connectionObserver = object : ConnectionObserver {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                Timber.v("onDeviceConnecting()")
                _data.tryEmit(ConnectingResult(device))
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
                Timber.v("onDeviceConnected()")
                _data.tryEmit(ConnectedResult(device))
            }

            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                Timber.e("onDeviceFailedToConnect(), reason: $reason")
                _data.tryEmit(MissingServiceResult(device))
            }

            override fun onDeviceReady(device: BluetoothDevice) {
                Timber.v("onDeviceReady()")
                _data.tryEmit(ReadyResult(device))
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                Timber.w("onDeviceDisconnecting()")
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                Timber.e("onDeviceDisconnected(), reason: $reason")
                _data.tryEmit(
                    when (reason) {
                        ConnectionObserver.REASON_NOT_SUPPORTED -> MissingServiceResult(device)
                        ConnectionObserver.REASON_LINK_LOSS -> LinkLossResult(device, null)
                        ConnectionObserver.REASON_SUCCESS -> DisconnectedResult(device, reason)
                        else -> UnknownErrorResult(device)
                    }
                )
            }
        }
    }

    override fun log(priority: Int, message: String) {
//        logger.log(priority, message)
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
                Timber.e("requestMtu  error:${device.name} $status")
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
                    _data.emit(UnknownErrorResult(device))
                }
            }
            .launchIn(scope)

        enableNotifications(notifyCharacteristic)
            .fail { device, status ->
                Timber.e("enableNotifications  error:${device.name} $status")
            }
            .enqueue()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val discoveryResult = deviceSpecManager.identifyDevice(gatt)

        return if (discoveryResult != null) {
            // 保存发现的设备信息
            notifyCharacteristic = discoveryResult.notifyCharacteristic
            writeCharacteristic = discoveryResult.writeCharacteristic

            Timber.d("设备识别成功: ${discoveryResult.spec.deviceName}")
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
     * 处理指令响应
     */
    private suspend fun processCommandResponse(commandResponse: CommandResponse) {
        if (commandResponse.latestResponse.isEmpty()) {
            Timber.w("收到空响应内容")
            return
        }
        
        // 处理成功响应
        val successResult = SuccessResult(
            device,
            CommandData(response = commandResponse.latestResponse)
        )

        _data.emit(successResult)

        // 如果启用了响应驱动的流控，触发下一个指令发送
        if (sendConfig.useResponseBasedFlow) {
            lastResponseTime = System.currentTimeMillis()
            processNextCommand()
        }

    }

    /**
     * 发送数据 - 改进版本，支持智能流控
     */
    fun sendData(command: String) {
        if (command.isBlank()) {
            Timber.w("尝试发送空指令，已忽略")
            return
        }

        val pendingCommand = PendingCommand(command)
        commandQueue.offer(pendingCommand)

        Timber.v("指令已加入队列: $command, 队列大小: ${commandQueue.size}")

        // 如果当前没有正在处理队列，则开始处理
        if (!isProcessingQueue) {
            scope.launch {
                processCommandQueue()
            }
        }
    }

    /**
     * 立即发送数据（不使用队列，用于紧急指令）
     */
    fun sendDataImmediate(command: String) {
        scope.launch {
            sendDataInternal(command)
        }
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
                val pendingCommand = commandQueue.poll() ?: break

                // 检查指令是否超时
                val isExpired =
                    System.currentTimeMillis() - pendingCommand.timestamp > sendConfig.responseTimeoutMs
                if (isExpired) {
                    Timber.w("指令已过期，跳过: ${pendingCommand.command}")
                    continue
                }

                val success = sendDataInternal(pendingCommand.command)

                // 成功发送或达到最大重试次数，使用正常的流控机制
                if (sendConfig.useResponseBasedFlow) {
                    waitForResponseOrTimeout()
                } else {
                    delay(sendConfig.fallbackDelayMs)
                }
            }
        } finally {
            isProcessingQueue = false
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
                Timber.v("收到响应，继续处理下一个指令")
                return
            }
            delay(50) // 短暂等待
        }

        Timber.w("等待响应超时，使用备用延时继续")
        delay(sendConfig.fallbackDelayMs)
    }

    /**
     * 内部发送数据方法
     */
    private suspend fun sendDataInternal(command: String): Boolean {
        return queueMutex.withLock {
            try {
                writeCharacteristic?.let { characteristic ->
                    Timber.v("发送数据: length=${command.toByteArray().size} bytes;content: $command")

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
     * 清空指令队列
     */
    fun clearCommandQueue() {
        commandQueue.clear()
        Timber.i("指令队列已清空")
    }

    /**
     * 获取队列状态
     */
    fun getQueueStatus(): Pair<Int, Boolean> {
        return commandQueue.size to isProcessingQueue
    }

    suspend fun connect() {
        try {
            connect(device)
                .useAutoConnect(false)
                // Automatic retries are supported, in case of 133 error.
                .retry(3, 300)
                // A connection timeout can be set. This is additional to the Android's connection timeout which is 30 seconds.
                .timeout(15_000)
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
}
