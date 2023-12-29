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
import com.blankj.utilcode.util.Utils
import com.shmedo.lib.ble.communicate.data.CommandData
import com.shmedo.lib.ble.communicate.data.MedoBleManager
import com.shmedo.lib.ble.communicate.service.base.BleManagerResult
import com.shmedo.lib.ble.communicate.service.base.ServiceManager
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class MedoBleRepository private constructor(
    private val context: Context,
    private val serviceManager: ServiceManager
) {
    private var medoBleManager: MedoBleManager? = null

    //    private val _data = MutableStateFlow<BleManagerResult<IOTCmdData>>(IdleResult())
//    val data = _data.asStateFlow()
    private val _data = MutableSharedFlow<BleManagerResult<CommandData>>()
    val data = _data.asSharedFlow()


    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: DiscoveredBluetoothDevice) {
        Timber.v("Medo BluetoothGatt: call startService MedoBleService")
        serviceManager.startService(MedoBleService::class.java, device)
    }

    fun startConnect(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val manager = MedoBleManager(context, scope)
        this.medoBleManager = manager

        manager.dataHolder.status.onEach {
//            _data.value = it
            _data.emit(it)

        }.launchIn(scope)

        scope.launch {
            Timber.v("Medo BluetoothGatt:call connect()")
            manager.connect(device)
        }
    }

    suspend fun sendData(command: String) {
        medoBleManager?.sendData(command)
    }

    fun disconnect() {
        medoBleManager?.release()
        medoBleManager = null
    }

    fun isConnected(): Boolean {
        return medoBleManager?.isReady ?: false
    }

    companion object {
        val instance: MedoBleRepository by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MedoBleRepository(Utils.getApp(), ServiceManager(Utils.getApp()))
        }
    }
}
