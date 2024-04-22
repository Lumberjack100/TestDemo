package com.shmedo.lib.ble.communicate.service

import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import com.shmedo.lib.core.util.ForegroundNotification
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
abstract class BleNotificationService : LifecycleService()  {
    override fun onCreate() {
        Timber.v("BleNotificationService onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        ForegroundNotification.startBleConnectForeground(this)
        return result
    }

    override fun onDestroy() {
        Timber.d("BleNotificationService onDestroy")
        ForegroundNotification.cancelNotification(ForegroundNotification.BLE_NOTIFICATION_ID)
        stopForegroundService()
        super.onDestroy()
    }

    /**
     * Stops the service as a foreground service
     */
    private fun stopForegroundService() {
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            ForegroundNotification.cancelNotification(ForegroundNotification.BLE_NOTIFICATION_ID)
        }
    }
}