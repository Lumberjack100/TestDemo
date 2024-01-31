package com.shmedo.lib.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.shmedo.lib.core.R

/**
 * 创建者：gonghe
 * 创建时间：2024/1/26
 * 描述： TODO
 */
object ForegroundNotification {
    private const val BLE_CHANNEL_ID = "FOREGROUND_BLE_SERVICE"
    const val BLE_NOTIFICATION_ID = 200


    private fun createChannelIfNeeded(
        channelID: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        descriptionContent: String? = StringUtils.getString(R.string.app_name)
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel =
            NotificationChannel(
                channelID,
                channelName,
                importance
            )
                .apply {
                    description = descriptionContent
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                    setSound(null, null)
                    vibrationPattern = longArrayOf(0)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
        NotificationManagerCompat.from(Utils.getApp())
            .createNotificationChannel(channel)
    }

    fun startBleConnectForeground(
        service: Service
    ) {
        createChannelIfNeeded(
            BLE_CHANNEL_ID,
            StringUtils.getString(R.string.ble_channel_connected_devices_title),
            NotificationManager.IMPORTANCE_LOW,
            StringUtils.getString(R.string.ble_channel_connected_devices_description)
        )
        val intent: Intent? = Utils.getApp().packageManager.getLaunchIntentForPackage(Utils.getApp().packageName)
        val pendingIntent = PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(service, BLE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(StringUtils.getString(R.string.ble_channel_connected_devices_title))
            .setContentText( StringUtils.getString(R.string.ble_notification_connected_message, "Device"))
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        service.startForeground(BLE_NOTIFICATION_ID, notification)
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    fun cancelNotification(notificationID: Int) {
        NotificationManagerCompat.from(Utils.getApp()).cancel(notificationID)
    }
}