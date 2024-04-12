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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.shmedo.lib.ble.communicate.parser.CommandResponse
import com.shmedo.lib.ble.communicate.service.base.BleManagerResult
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.ReadyResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
import com.shmedo.lib.ble.communicate.spec.ESP32ASpec
import com.shmedo.lib.ble.communicate.spec.ESP32BSpec
import com.shmedo.lib.ble.communicate.spec.GOC400Spec
import com.shmedo.lib.ble.communicate.spec.GOCW91200Spec
import com.shmedo.lib.ble.communicate.spec.MS52SF1Spec
import com.shmedo.lib.ble.communicate.spec.PacketMerger
import com.shmedo.lib.ble.communicate.spec.USRSpec
import com.shmedo.lib.core.ext.addIOTDeviceLogItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.observer.ConnectionObserver
import timber.log.Timber


class MedoBleManager(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val _data = MutableSharedFlow<BleManagerResult<CommandData>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val data = _data.asSharedFlow()


    init {
        connectionObserver = object : ConnectionObserver {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                Timber.v("onDeviceConnecting()")
                addIOTDeviceLogItem(
                    priority = Log.INFO,
                    data = "device ${device.address} connecting",
                    scope
                )
                _data.tryEmit(ConnectingResult(device))
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
                Timber.v("onDeviceConnected()")
                addIOTDeviceLogItem(priority = Log.INFO, data = "device connected", scope)
                _data.tryEmit(ConnectedResult(device))
            }

            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                Timber.e("onDeviceFailedToConnect(), reason: $reason")
                addIOTDeviceLogItem(
                    priority = Log.ERROR,
                    data = "device failed to connect, reason: $reason",
                    scope
                )
                _data.tryEmit(MissingServiceResult(device))
            }

            override fun onDeviceReady(device: BluetoothDevice) {
                Timber.v("onDeviceReady()")
                addIOTDeviceLogItem(priority = Log.INFO, data = "device ready", scope)
                _data.tryEmit(ReadyResult(device))
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                Timber.w("onDeviceDisconnecting()")
                addIOTDeviceLogItem(priority = Log.WARN, data = "device disconnecting", scope)
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                Timber.e("onDeviceDisconnected(), reason: $reason")
                addIOTDeviceLogItem(
                    priority = Log.ERROR,
                    data = "device disconnected, reason: $reason",
                    scope
                )
                _data.tryEmit(
                    when (reason) {
                        ConnectionObserver.REASON_NOT_SUPPORTED -> MissingServiceResult(device)
                        ConnectionObserver.REASON_LINK_LOSS -> LinkLossResult(device, null)
                        ConnectionObserver.REASON_SUCCESS -> DisconnectedResult(device)
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


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        // Increase the MTU
        requestMtu(512).enqueue()

        // Enable notifications
        setNotificationCallback(notifyCharacteristic)
            // Merges packets until the entire text is present in the stream [PacketMerger.merge].
            .merge(PacketMerger())
            .asValidResponseFlow<CommandResponse>()
            .onEach {
                _data.emit(
                    SuccessResult(
                        device,
                        CommandData(response = it.response, responseList = it.responseList)
                    )
                )
            }
            .launchIn(scope)

        enableNotifications(notifyCharacteristic).enqueue()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        gatt.getService(ESP32ASpec.ESP32_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                ESP32ASpec.ESP32_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                ESP32ASpec.ESP32_WRITABLE_CHARACTERISTIC_UUID
            )
        }
        gatt.getService(ESP32BSpec.ESP32B_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                ESP32BSpec.ESP32B_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                ESP32BSpec.ESP32B_WRITABLE_CHARACTERISTIC_UUID
            )
        }
        gatt.getService(GOC400Spec.GOC400_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                GOC400Spec.GOC400_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                GOC400Spec.GOC400_WRITABLE_CHARACTERISTIC_UUID
            )
        }
        gatt.getService(GOCW91200Spec.GOCW91200_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                GOCW91200Spec.GOCW91200_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                GOCW91200Spec.GOCW91200_WRITABLE_CHARACTERISTIC_UUID
            )
        }
        gatt.getService(USRSpec.USR_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                USRSpec.USR_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                USRSpec.USR_WRITABLE_CHARACTERISTIC_UUID
            )
        }
        gatt.getService(MS52SF1Spec.MS52SF1_SERVICE_UUID)?.run {
            notifyCharacteristic = getCharacteristic(
                MS52SF1Spec.MS52SF1_NOTIFY_CHARACTERISTIC_UUID
            )
            writeCharacteristic = getCharacteristic(
                MS52SF1Spec.MS52SF1_WRITABLE_CHARACTERISTIC_UUID
            )
        }

        var writeRequest = false
        var writeCommand = false
        writeCharacteristic?.let {
            val rxProperties = it.properties
            writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
            writeCommand =
                rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0
        }

        val supported =
            notifyCharacteristic != null && writeCharacteristic != null && (writeRequest || writeCommand)
        return supported
    }

    override fun onServicesInvalidated() {
        writeCharacteristic = null
        notifyCharacteristic = null
    }

    suspend fun sendData(command: String) {
        writeCharacteristic?.let {
            Timber.v(
                "发送数据: length=%s bytes;content: %s",
                command.toByteArray().size,
                command
            )
            writeCharacteristic(
                it,
                Data.from(command),
                it.writeType
            )
                // Outgoing data can use automatic splitting.
                //.split() with no parameters uses the default MTU splitter.
                .split()
                .suspend()
        }
    }

    suspend fun connect() = connect(device)
        .useAutoConnect(false)
        // Automatic retries are supported, in case of 133 error.
        .retry(3, 300)
        // A connection timeout can be set. This is additional to the Android's connection timeout which is 30 seconds.
        .timeout(15_000)
        // To suspend until the connection AND initialization is complete, call suspend().
        .suspend()

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}
