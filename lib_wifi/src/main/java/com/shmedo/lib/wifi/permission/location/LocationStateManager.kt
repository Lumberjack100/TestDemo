package com.shmedo.lib.wifi.permission.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.shmedo.lib.wifi.permission.WifiPermissionNotAvailableReason
import com.shmedo.lib.wifi.permission.WifiPermissionState
import com.shmedo.lib.wifi.permission.util.WifiPermissionUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * 定位状态管理器（WiFi 专用）
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 监听定位服务状态变化，自动通知订阅者
 *       WiFi 扫描在 Android 10+ 需要定位权限和定位服务开启
 */
private const val REFRESH_PERMISSIONS = "com.shmedo.lib.wifi.permission.REFRESH_LOCATION_PERMISSIONS"

class LocationStateManager(private val context: Context) {

    /**
     * 定位状态流
     * 
     * 使用 callbackFlow 监听系统定位服务状态变化广播，
     * 当定位服务开关状态改变时自动更新流
     */
    fun locationState() = callbackFlow {
        // 立即发送当前状态
        trySend(getLocationState())

        // 注册广播接收器监听定位状态变化
        val locationStateChangeHandler = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(getLocationState())
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(LocationManager.MODE_CHANGED_ACTION)
            addAction(REFRESH_PERMISSIONS)
        }
        
        ContextCompat.registerReceiver(
            context,
            locationStateChangeHandler,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        
        awaitClose {
            context.unregisterReceiver(locationStateChangeHandler)
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
     * 获取当前定位权限状态
     * 
     * @return 定位权限状态
     */
    private fun getLocationState(): WifiPermissionState {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return when {
            // 检查定位服务是否开启
            !LocationManagerCompat.isLocationEnabled(lm) ->
                WifiPermissionState.NotAvailable(WifiPermissionNotAvailableReason.LocationServiceDisabled)

            // 检查定位权限是否授予
            !WifiPermissionUtil.isLocationPermissionGranted ->
                WifiPermissionState.NotAvailable(WifiPermissionNotAvailableReason.PermissionRequired)

            else -> WifiPermissionState.Available
        }
    }
}

