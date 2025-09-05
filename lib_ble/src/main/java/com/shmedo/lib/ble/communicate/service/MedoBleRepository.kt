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

package com.shmedo.lib.ble.communicate.service

import android.content.Context
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.data.EnhancedMedoBleManager
import com.shmedo.lib.ble.communicate.data.SessionCommand
import com.shmedo.lib.ble.communicate.service.base.ServiceManager
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import timber.log.Timber

class MedoBleRepository(
    private val context: Context,
    private val serviceManager: ServiceManager
) {
    private var enhancedMedoBleManager: EnhancedMedoBleManager? = null

    // 分离的连接状态流
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState = _connectionState.asSharedFlow()
    
    // 分离的数据流
    private val _commandData = MutableSharedFlow<CommandData>()
    val commandData = _commandData.asSharedFlow()

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(MedoBleService::class.java, device)
    }

    fun startConnect(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        // 使用增强版管理器
        val enhancedManager = EnhancedMedoBleManager(context, scope, device.device)
        this.enhancedMedoBleManager = enhancedManager

        // 收集连接状态
        enhancedManager.connectionState.onEach { state ->
            _connectionState.emit(state)
        }.launchIn(scope)

        // 收集数据响应（兼容原有接口）
        enhancedManager.commandData.onEach { commandData ->
            _commandData.emit(commandData)
        }.launchIn(scope)

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Timber.e("MedoBle Error", throwable)
        }
        scope.launch(Dispatchers.IO + exceptionHandler) {
            Timber.v("EnhancedMedoBle call connect()")
            enhancedManager.connect()
        }
    }

    /**
     * 兼容原有接口：发送普通指令
     */
    fun sendData(command: String) {
        enhancedMedoBleManager?.sendData(command)
    }

    /**
     * 新接口：发送会话指令
     */
    fun sendSessionCommand(sessionCommand: SessionCommand) {
        enhancedMedoBleManager?.sendSessionCommand(sessionCommand)
    }

    /**
     * 注册会话监听器
     */
    suspend fun registerSessionListener(sessionId: String, listener: (CommandData) -> Unit) {
        enhancedMedoBleManager?.registerSessionListener(sessionId, listener)
    }

    /**
     * 注销会话监听器
     */
    suspend fun unregisterSessionListener(sessionId: String) {
        enhancedMedoBleManager?.unregisterSessionListener(sessionId)
    }

    /**
     * 取消会话
     */
    fun cancelSession(sessionId: String) {
        enhancedMedoBleManager?.cancelSession(sessionId)
    }

    /**
     * 获取会话状态
     */
    fun getSessionStatus(): Triple<Int, Int, Int>? {
        return enhancedMedoBleManager?.getSessionStatus()
    }

    fun disconnect() {
        enhancedMedoBleManager?.release()
        enhancedMedoBleManager = null
    }

    fun isConnected(): Boolean {
        return enhancedMedoBleManager?.isReady ?: false
    }
}
