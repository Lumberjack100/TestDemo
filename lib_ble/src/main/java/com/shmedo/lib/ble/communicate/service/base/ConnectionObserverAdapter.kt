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

package com.shmedo.lib.ble.communicate.service.base

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.shmedo.lib.core.ext.addIOTDeviceLogItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.observer.ConnectionObserver
import timber.log.Timber

class ConnectionObserverAdapter<T>(private val scope: CoroutineScope) : ConnectionObserver {

    private val _status = MutableStateFlow<BleManagerResult<T>>(IdleResult())
    val status = _status.asStateFlow()

    private var lastValue: T? = null

    private fun getData(): T? {
        return (_status.value as? SuccessResult)?.data
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Timber.v("onDeviceConnecting()")
        addIOTDeviceLogItem(priority = Log.INFO, data = "device ${device.address} connecting", scope)
        _status.value = ConnectingResult(device)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Timber.v("onDeviceConnected()")
        addIOTDeviceLogItem(priority = Log.INFO, data = "device connected", scope)
        _status.value = ConnectedResult(device)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Timber.e("onDeviceFailedToConnect(), reason: $reason")
        addIOTDeviceLogItem(priority = Log.ERROR, data = "device failed to connect, reason: $reason", scope)
        _status.value = MissingServiceResult(device)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Timber.v("onDeviceReady()")
        addIOTDeviceLogItem(priority = Log.INFO, data = "device ready", scope)
        _status.value = ReadyResult(device)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Timber.w("onDeviceDisconnecting()")
        addIOTDeviceLogItem(priority = Log.WARN, data = "device disconnecting", scope)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Timber.e("onDeviceDisconnected(), reason: $reason")
        addIOTDeviceLogItem(priority = Log.ERROR, data = "device disconnected, reason: $reason", scope)
        _status.value = when (reason) {
            ConnectionObserver.REASON_NOT_SUPPORTED -> MissingServiceResult(device)
            ConnectionObserver.REASON_LINK_LOSS -> LinkLossResult(device, getData())
            ConnectionObserver.REASON_SUCCESS -> DisconnectedResult(device)
            else -> UnknownErrorResult(device)
        }
    }

    fun setValue(value: T) {
        lastValue = value

        when (val currentValue = _status.value) {
            is ReadyResult -> {
                _status.value = SuccessResult(currentValue.device, value)
            }

            is SuccessResult -> {
                _status.value = SuccessResult(currentValue.device, value)

                getData()?.let {
                    addIOTDeviceLogItem(priority = Log.INFO, data = it.toString(), scope)
                }
            }

            else -> {}
        }
    }
}
