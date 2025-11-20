package com.shmedo.lib.wifi.scanner.model

import android.net.wifi.ScanResult
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 已发现的 WiFi 网络
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: WiFi 扫描结果封装，类似于 BLE 的 DiscoveredBluetoothDevice
 */
@Parcelize
data class DiscoveredWifiNetwork(
    val ssid: String,              // 网络名称
    val bssid: String,             // MAC 地址
    val capabilities: String,      // 加密类型
    val level: Int,                // 信号强度 (dBm)
    val frequency: Int,            // 频率 (MHz)
    val timestamp: Long,           // 发现时间戳
    val scanResult: ScanResult? = null // 原始扫描结果
) : Parcelable {
    
    /**
     * 信号强度百分比 (0-100)
     */
    val signalStrength: Int
        get() = calculateSignalLevel(level, 100)
    
    /**
     * 是否需要密码
     */
    val isSecured: Boolean
        get() = capabilities.contains("WPA") || 
                capabilities.contains("WEP") || 
                capabilities.contains("PSK")
    
    /**
     * 是否为 5G 频段
     */
    val is5GHz: Boolean
        get() = frequency in 5150..5825
    
    /**
     * 频段描述
     */
    val band: String
        get() = if (is5GHz) "5GHz" else "2.4GHz"
    
    /**
     * 信号质量描述
     */
    val signalQuality: String
        get() = when {
            level >= -50 -> "优秀"
            level >= -60 -> "良好"
            level >= -70 -> "一般"
            else -> "较差"
        }
    
    /**
     * 计算信号强度等级
     */
    private fun calculateSignalLevel(rssi: Int, numLevels: Int): Int {
        return when {
            rssi <= -100 -> 0
            rssi >= -55 -> numLevels - 1
            else -> {
                val inputRange = (-55 - -100).toFloat()
                val outputRange = (numLevels - 1).toFloat()
                ((rssi + 100) * outputRange / inputRange).toInt()
            }
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscoveredWifiNetwork) return false
        return bssid == other.bssid
    }
    
    override fun hashCode(): Int {
        return bssid.hashCode()
    }
}

