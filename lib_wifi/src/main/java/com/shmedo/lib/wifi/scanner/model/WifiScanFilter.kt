package com.shmedo.lib.wifi.scanner.model

/**
 * WiFi 扫描过滤器配置
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 用于过滤和筛选 WiFi 扫描结果
 */
data class WifiScanFilter(
    val filterStrongSignalOnly: Boolean = false, // 仅显示强信号 (-70dBm 以上)
    val filterBySSID: String? = null,            // 按 SSID 过滤
    val showAllAccessPoints: Boolean = false     // 是否显示同名网络的所有接入点（默认每个 SSID 仅显示信号最强的）
)

