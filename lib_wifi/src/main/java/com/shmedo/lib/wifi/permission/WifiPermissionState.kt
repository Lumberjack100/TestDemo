package com.shmedo.lib.wifi.permission

/**
 * WiFi 权限状态
 *
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: Android 10+ WiFi 扫描需要：
 *  1. ACCESS_FINE_LOCATION 权限
 *  2. 定位服务开启
 *  3. WiFi 开启
 */
sealed class WifiPermissionState {
    /**
     * 权限可用
     */
    data object Available : WifiPermissionState()

    /**
     * 权限不可用
     * @param reason 不可用的原因
     */
    data class NotAvailable(val reason: WifiPermissionNotAvailableReason) : WifiPermissionState()
}

/**
 * WiFi 权限不可用原因
 */
sealed class WifiPermissionNotAvailableReason {
    /**
     * 定位服务未开启
     */
    object LocationServiceDisabled : WifiPermissionNotAvailableReason()

    /**
     * 缺少权限
     */
    object PermissionRequired : WifiPermissionNotAvailableReason()

    /**
     * WiFi 未开启
     */
    object WifiDisabled : WifiPermissionNotAvailableReason()
}

