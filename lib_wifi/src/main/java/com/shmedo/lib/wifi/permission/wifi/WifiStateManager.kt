package com.shmedo.lib.wifi.permission.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.shmedo.lib.wifi.permission.WifiPermissionNotAvailableReason
import com.shmedo.lib.wifi.permission.WifiPermissionState
import com.shmedo.lib.wifi.permission.util.WifiPermissionUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * WiFi 状态管理器
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 监听 WiFi 状态变化，自动通知订阅者
 */
private const val REFRESH_PERMISSIONS = "com.shmedo.lib.wifi.permission.REFRESH_WIFI_PERMISSIONS"

class WifiStateManager(private val context: Context) {

    /**
     * WiFi 状态流
     * 
     * 使用 callbackFlow 监听系统 WiFi 状态变化广播，
     * 当 WiFi 开关状态改变时自动更新流
     */
    fun wifiState() = callbackFlow {
        // 立即发送当前状态
        trySend(getWifiPermissionState())

        // 注册广播接收器监听 WiFi 状态变化
        val wifiStateChangeHandler = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(getWifiPermissionState())
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(REFRESH_PERMISSIONS)
        }
        
        ContextCompat.registerReceiver(
            context,
            wifiStateChangeHandler,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        
        awaitClose {
            context.unregisterReceiver(wifiStateChangeHandler)
        }
    }

    /**
     * 手动刷新权限状态
     * 
     * 发送自定义广播触发状态更新，
     * 用于权限授予后手动刷新
     */
    fun refreshPermission() {
        val intent = Intent(REFRESH_PERMISSIONS)
        context.sendBroadcast(intent)
    }

    /**
     * 获取当前 WiFi 权限状态
     * 
     * @return WiFi 权限状态
     */
    private fun getWifiPermissionState() = when {
        !WifiPermissionUtil.isWifiAvailable -> WifiPermissionState.NotAvailable(
            WifiPermissionNotAvailableReason.WifiDisabled
        )
        else -> WifiPermissionState.Available
    }
}

