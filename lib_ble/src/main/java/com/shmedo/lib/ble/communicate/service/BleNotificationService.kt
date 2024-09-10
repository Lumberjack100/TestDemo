package com.shmedo.lib.ble.communicate.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.shmedo.lib.ble.R
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/19
 * 描述： TODO
 */
abstract class BleNotificationService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        Timber.d("BleNotificationService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        Timber.d("BleNotificationService onStartCommand")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                BLE_NOTIFICATION_ID,
                createForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(BLE_NOTIFICATION_ID, createForegroundNotification())
        }
        return result
    }

    override fun onDestroy() {
        Timber.d("BleNotificationService onDestroy")
        NotificationManagerCompat.from(this@BleNotificationService).cancel(BLE_NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun createForegroundNotification(): Notification {
        createChannelIfNeeded()
        val builder = NotificationCompat.Builder(this@BleNotificationService, BLE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(StringUtils.getString(R.string.ble_channel_connected_devices_title))
            .setContentText(
                StringUtils.getString(
                    R.string.ble_notification_connected_message,
                    "Device"
                )
            )
            .setContentIntent(getOpenAppIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // 设置通知优先级
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(true)

        return builder.build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        NotificationChannel(
            BLE_CHANNEL_ID,
            "BleNotificationService",
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                NotificationManagerCompat.from(Utils.getApp()).createNotificationChannel(this)
            }
    }

    private fun getOpenAppIntent(): PendingIntent {
        val intent: Intent? =
            Utils.getApp().packageManager.getLaunchIntentForPackage(Utils.getApp().packageName)
        val pendingIntent =
            PendingIntent.getActivity(
                this@BleNotificationService,
                BLE_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        return pendingIntent
    }

    companion object {
        const val BLE_CHANNEL_ID = "com.shmedo.mcloudapp.BleNotificationService"
        const val BLE_NOTIFICATION_ID = 12000
    }
}