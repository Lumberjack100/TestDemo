package com.shmedo.lib.wifi.permission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.wifi.permission.WifiPermissionManager
import com.shmedo.lib.wifi.permission.WifiPermissionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * WiFi 权限 ViewModel
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 暴露权限状态给 UI 层
 */
class WifiPermissionViewModel(
    private val permissionManager: WifiPermissionManager
) : ViewModel() {
    
    /**
     * 权限状态流
     */
    val permissionState: StateFlow<WifiPermissionState> = permissionManager.permissionState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WifiPermissionState.Checking
        )
    
    /**
     * 检查权限
     */
    fun checkPermissions() {
        permissionManager.checkPermissions()
    }
}

