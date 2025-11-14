package com.shmedo.lib.wifi.permission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.wifi.permission.WifiPermissionNotAvailableReason
import com.shmedo.lib.wifi.permission.WifiPermissionState
import com.shmedo.lib.wifi.permission.location.LocationStateManager
import com.shmedo.lib.wifi.permission.wifi.WifiStateManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * WiFi 权限 ViewModel
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 暴露权限状态给 UI 层，整合 WiFi 状态和定位状态管理器
 */
class WifiPermissionViewModel(
    private val wifiStateManager: WifiStateManager,
    private val locationStateManager: LocationStateManager
) : ViewModel() {
    
    /**
     * WiFi 状态流
     * 
     * 监听 WiFi 开关状态变化
     */
    val wifiState = wifiStateManager.wifiState()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            WifiPermissionState.NotAvailable(WifiPermissionNotAvailableReason.WifiDisabled)
        )
    
    /**
     * 定位权限状态流
     * 
     * 监听定位权限和定位服务状态变化
     */
    val locationState = locationStateManager.locationState()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            WifiPermissionState.NotAvailable(WifiPermissionNotAvailableReason.PermissionRequired)
        )

    /**
     * 刷新 WiFi 权限状态
     * 
     * 手动触发 WiFi 状态更新，
     * 用于从设置页面返回后刷新状态
     */
    fun refreshWifiPermission() {
        wifiStateManager.refreshPermission()
    }

    /**
     * 刷新定位权限状态
     * 
     * 手动触发定位状态更新，
     * 用于权限授予后刷新状态
     */
    fun refreshLocationPermission() {
        locationStateManager.refreshPermission()
    }

}

