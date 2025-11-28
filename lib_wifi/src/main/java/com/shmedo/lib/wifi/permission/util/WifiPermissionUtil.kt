package com.shmedo.lib.wifi.permission.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.blankj.utilcode.util.Utils

/**
 * WiFi 权限工具类
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 提供 WiFi 和定位相关的权限检查工具方法
 */
object WifiPermissionUtil {

    /**
     * 检查 WiFi 是否开启
     */
    val isWifiAvailable: Boolean
        get() = try {
            val wifiManager = Utils.getApp().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            false
        }

    /**
     * 检查定位服务是否开启
     */
    val isLocationEnabled: Boolean
        get() = try {
            val lm = Utils.getApp().getSystemService(LocationManager::class.java)
            LocationManagerCompat.isLocationEnabled(lm)
        } catch (e: Exception) {
            false
        }

    /**
     * 检查定位权限是否已授予
     * 
     * WiFi 扫描在 Android 10+ 需要 ACCESS_FINE_LOCATION 权限
     */
    val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            Utils.getApp(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

