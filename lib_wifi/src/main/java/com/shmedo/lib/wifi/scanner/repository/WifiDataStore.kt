package com.shmedo.lib.wifi.scanner.repository

import com.shmedo.lib.wifi.scanner.model.DiscoveredWifiNetwork
import timber.log.Timber

/**
 * WiFi 数据存储
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 管理已发现的 WiFi 网络列表，去重和更新
 */
internal class WifiDataStore {
    
    private val _networks = mutableMapOf<String, DiscoveredWifiNetwork>() // key: BSSID
    
    /**
     * 获取所有网络，按信号强度降序排列
     */
    val networks: List<DiscoveredWifiNetwork>
        get() = _networks.values
            .sortedByDescending { it.level }
    
    /**
     * 添加或更新 WiFi 网络
     */
    fun addOrUpdateNetwork(network: DiscoveredWifiNetwork) {
        val existing = _networks[network.bssid]
        
        if (existing == null) {
            _networks[network.bssid] = network
            Timber.d("新发现 WiFi: ${network.ssid} (${network.bssid}), 信号: ${network.level}dBm")
        } else {
            // 更新信号强度
            if (existing.level != network.level) {
                _networks[network.bssid] = network
                Timber.v("更新 WiFi 信号: ${network.ssid}, ${existing.level}dBm -> ${network.level}dBm")
            }
        }
    }
    
    /**
     * 清空所有网络
     */
    fun clear() {
        _networks.clear()
        Timber.d("清空 WiFi 列表")
    }
    
    /**
     * 获取网络数量
     */
    fun size(): Int = _networks.size
}

