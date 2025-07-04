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

import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.Utils
import com.shmedo.lib.ble.R
import com.shmedo.lib.ble.communicate.service.base.DEVICE_DATA
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

internal class MedoBleService : BleNotificationService() {
    private val medoBleRepository: MedoBleRepository by inject { parametersOf(this) }
    private var device: DiscoveredBluetoothDevice? = null
    private var hasBeenConnected = false  //标记是否曾经连接过

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        device = intent?.getParcelableExtra<DiscoveredBluetoothDevice>(DEVICE_DATA)

        if (device == null) {
            Timber.e("设备数据为空，停止服务")
            //在停止服务前清理通知
            safeStopService()
            return START_NOT_STICKY
        }

        // 监听连接状态
        medoBleRepository.connectionState.onEach { state ->
            Timber.d("BLE Service 状态变化: $state")
            handleConnectionState(state)
        }.launchIn(lifecycleScope)

        medoBleRepository.startConnect(device!!, lifecycleScope)

        return START_NOT_STICKY
    }

    /**
     * 处理连接状态变化
     */
    private fun handleConnectionState(state: ConnectionState) {
        try {
            when (state) {
                ConnectionState.Connecting -> {
                    updateNotification("正在连接...")
                }

                is ConnectionState.Initializing -> {
                    hasBeenConnected = true  // 标记已经开始连接过程
                    updateNotification("正在初始化...")
                }

                is ConnectionState.Ready -> {
                    hasBeenConnected = true // 标记已经连接
                    updateNotification("已连接到 ${device?.name ?: device?.address}")
                }

                ConnectionState.Disconnecting -> {
                    updateNotification("正在断开连接...")
                }

                is ConnectionState.Disconnected -> {
                    // 断开连接时立即清理通知并停止服务
                    if (hasBeenConnected) {
                        Timber.i("设备已断开连接: ${state.reason}")
                        safeStopService()
                    } else {
                        // 初始断开状态，不需要停止服务
                        Timber.d("设备初始状态为断开，等待连接...")
                        updateNotification("准备连接到 ${device?.name ?: device?.address}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "处理连接状态时发生异常")
            // 异常情况下也要安全停止服务
            safeStopService()
        }
    }

    /**
     * 重写 onDestroy 确保通知被清理
     */
    override fun onDestroy() {
        Timber.d("MedoBleService onDestroy")
        try {
            // 确保断开连接
            medoBleRepository.disconnect()
            //确保通知被清理（基类也会调用，但这里再次确保）
            clearNotification()
        } catch (e: Exception) {
            Timber.e(e, "MedoBleService onDestroy 时发生异常")
        } finally {
            super.onDestroy()
        }
    }

    // 实现真正的通知更新
    override fun updateNotification(message: String) {
        try {
            val notification = NotificationCompat.Builder(this, BLE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("蓝牙设备连接")
                .setContentText(message)
                .setContentIntent(getOpenAppIntent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            NotificationManagerCompat.from(Utils.getApp()).notify(BLE_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Timber.e(e, "更新通知时发生异常: $message")
        }
    }
}
