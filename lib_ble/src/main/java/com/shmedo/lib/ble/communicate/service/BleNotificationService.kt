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
 * 描述：蓝牙通知服务基类，提供前台服务和通知管理功能
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

    /**
     * 获取打开应用的 PendingIntent
     */
    protected fun getOpenAppIntent(): PendingIntent {
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

    /**
     * 清理通知
     */
    protected fun clearNotification() {
        NotificationManagerCompat.from(this@BleNotificationService).cancel(BLE_NOTIFICATION_ID)
        Timber.d("通知已清理")
    }

    /**
     * 抽象方法：更新通知内容，子类必须实现
     */
    protected abstract fun updateNotification(message: String)


    /**
     * 安全停止服务 - 确保通知被清理
     */
    protected fun safeStopService() {
        try {
            // 先清理通知
            clearNotification()
            Timber.d("服务停止前已清理通知")

            // 再停止服务
            stopSelf()
        } catch (e: Exception) {
            Timber.e(e, "停止服务时发生异常")
            // 即使出现异常也要尝试清理通知
            try {
                clearNotification()
            } catch (clearException: Exception) {
                Timber.e(clearException, "清理通知时发生异常")
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        Timber.d("BleNotificationService onDestroy")
        // 清理通知
        clearNotification()
        super.onDestroy()
    }

    companion object {
        const val BLE_CHANNEL_ID = "com.shmedo.mcloudapp.BleNotificationService"
        const val BLE_NOTIFICATION_ID = 12000
    }
}