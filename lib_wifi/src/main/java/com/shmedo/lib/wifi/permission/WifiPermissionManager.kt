package com.shmedo.lib.wifi.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * WiFi 权限管理器
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 管理 WiFi 扫描所需的各项权限
 */
@RequiresApi(Build.VERSION_CODES.Q)
class WifiPermissionManager(private val context: Context) {
    
    private val _permissionState = MutableStateFlow<WifiPermissionState>(WifiPermissionState.Checking)
    val permissionState: StateFlow<WifiPermissionState> = _permissionState
    
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    /**
     * 检查权限状态
     */
    fun checkPermissions() {
        Timber.d("检查 WiFi 权限状态")
        
        // 1. 检查 WiFi 是否开启
        if (!wifiManager.isWifiEnabled) {
            Timber.w("WiFi 未开启")
            _permissionState.value = WifiPermissionState.NotAvailable(
                WifiPermissionNotAvailableReason.WifiDisabled
            )
            return
        }
        
        // 2. 检查定位权限
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocationPermission) {
            Timber.w("缺少定位权限")
            _permissionState.value = WifiPermissionState.NotAvailable(
                WifiPermissionNotAvailableReason.PermissionRequired
            )
            return
        }
        
        // 3. 检查定位服务是否开启
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        if (!isLocationEnabled) {
            Timber.w("定位服务未开启")
            _permissionState.value = WifiPermissionState.NotAvailable(
                WifiPermissionNotAvailableReason.LocationServiceDisabled
            )
            return
        }
        
        // 所有权限已就绪
        Timber.i("WiFi 权限检查通过")
        _permissionState.value = WifiPermissionState.Available
    }
}

