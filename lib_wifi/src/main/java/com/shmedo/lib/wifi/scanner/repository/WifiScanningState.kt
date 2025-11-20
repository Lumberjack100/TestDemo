package com.shmedo.lib.wifi.scanner.repository

import com.shmedo.lib.wifi.scanner.model.DiscoveredWifiNetwork

/**
 * WiFi 扫描状态
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 参考 BLE 的 ScanningState 设计
 */
sealed class WifiScanningState {
    /**
     * 空闲状态
     */
    object Idle : WifiScanningState()
    
    /**
     * 扫描中
     */
    object Scanning : WifiScanningState()
    
    /**
     * 发现网络
     * @param networks 已发现的 WiFi 网络列表
     */
    data class NetworksDiscovered(val networks: List<DiscoveredWifiNetwork>) : WifiScanningState()
    
    /**
     * 扫描错误
     * @param errorMsg 错误消息
     */
    data class Error(val errorMsg: String) : WifiScanningState()
}

