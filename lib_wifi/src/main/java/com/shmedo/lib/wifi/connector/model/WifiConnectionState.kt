package com.shmedo.lib.wifi.connector.model

/**
 * WiFi 连接状态
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: WiFi 连接状态封装
 */
sealed class WifiConnectionState {
    /**
     * 空闲状态
     */
    object Idle : WifiConnectionState()
    
    /**
     * 连接中
     */
    object Connecting : WifiConnectionState()
    
    /**
     * 已连接
     * @param ssid 网络名称
     * @param ipAddress IP 地址
     */
    data class Connected(val ssid: String, val ipAddress: String) : WifiConnectionState()
    
    /**
     * 已断开
     */
    object Disconnected : WifiConnectionState()
    
    /**
     * 连接错误
     * @param message 错误消息
     */
    data class Error(val message: String) : WifiConnectionState()
}

