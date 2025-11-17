package com.shmedo.lib.wifi.scanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.wifi.scanner.model.WifiScanFilter
import com.shmedo.lib.wifi.scanner.repository.WifiScannerRepository
import com.shmedo.lib.wifi.scanner.repository.WifiScanningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber


private const val FILTER_RSSI = -70 // [dBm]


/**
 *
 * 创建者: gonghe
 * 创建时间: 2025/11/17
 *
 * 描述: WiFi 扫描 ViewModel，startScan() 方法支持手动触发扫描
 *
 */
class WifiScannerViewModel(
    private val scannerRepository: WifiScannerRepository
) : ViewModel() {
    
    private val _filter = MutableStateFlow(WifiScanFilter())
    
    /**
     * 扫描状态流，应用过滤器后的结果
     * 
     * 使用 WhileSubscribed(5000) 策略：
     * - 当有订阅者时保持活跃
     * - 没有订阅者后延迟 5 秒停止扫描
     * - 可以节省资源并自动管理生命周期
     */
    val scanningState = _filter
        .combine(scannerRepository.scanWifiNetworks()) { filter, state ->
            when (state) {
                is WifiScanningState.NetworksDiscovered -> state.applyFilter(filter)
                else -> state
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WifiScanningState.Idle
        )
    
    /**
     * 应用过滤器
     */
    private fun WifiScanningState.NetworksDiscovered.applyFilter(filter: WifiScanFilter): WifiScanningState.NetworksDiscovered {
        val filtered = networks
            .asSequence()
            .filter { network ->
                // 过滤强信号
                !filter.filterStrongSignalOnly || network.level >= FILTER_RSSI
            }
            .filter { network ->
                // 按 SSID 过滤
                filter.filterBySSID.isNullOrBlank() || 
                network.ssid.contains(filter.filterBySSID, ignoreCase = true)
            }
            .toList()
        
        Timber.d("过滤后网络数量: ${filtered.size} / ${networks.size}")
        
        return WifiScanningState.NetworksDiscovered(filtered)
    }
    
    /**
     * 设置过滤器
     */
    fun setFilter(filter: WifiScanFilter) {
        _filter.value = filter
    }
    
    /**
     * 按 SSID 搜索
     */
    fun searchBySSID(ssid: String) {
        _filter.value = _filter.value.copy(filterBySSID = ssid)
    }

    /**
     * 启动扫描
     * 
     * 触发一次新的 WiFi 扫描
     * 可在以下场景调用：
     * - 用户下拉刷新
     * - 权限授予后
     * - 页面初次加载
     */
    fun startScan() {
        Timber.i("ViewModel 触发扫描")
        scannerRepository.startScan()
    }
    
    override fun onCleared() {
        super.onCleared()
        scannerRepository.clear()
    }
}

