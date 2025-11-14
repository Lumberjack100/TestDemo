package com.shmedo.lib.wifi.connector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.wifi.connector.model.WifiConnectionState
import com.shmedo.lib.wifi.connector.repository.WifiConnectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * WiFi 连接 ViewModel
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: 管理 WiFi 连接状态和操作
 */
class WifiConnectorViewModel(
    private val connectorRepository: WifiConnectorRepository
) : ViewModel() {
    
    private val _connectionState = MutableStateFlow<WifiConnectionState>(WifiConnectionState.Idle)
    val connectionState: StateFlow<WifiConnectionState> = _connectionState
    
    // 存储自定义端口配置（用于后续 TCP 连接）
    private var customTcpPort: Int = DEFAULT_TCP_PORT
    
    companion object {
        const val DEFAULT_TCP_PORT = 8888 // 默认 TCP 端口
    }
    
    /**
     * 连接到 WiFi 网络
     * 
     * @param ssid 网络 SSID
     * @param password 密码
     * @param isOpen 是否为开放网络
     * @param tcpPort 自定义 TCP 端口（可选）
     */
    fun connect(
        ssid: String,
        password: String?,
        isOpen: Boolean = false,
        tcpPort: Int = DEFAULT_TCP_PORT
    ) {
        this.customTcpPort = tcpPort
        Timber.i("准备连接 WiFi: $ssid, TCP 端口: $tcpPort")
        
        connectorRepository.connectToWifi(ssid, password, isOpen)
            .onEach { state ->
                _connectionState.value = state
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        connectorRepository.disconnect()
        _connectionState.value = WifiConnectionState.Disconnected
    }
    
    /**
     * 获取自定义 TCP 端口
     */
    fun getTcpPort(): Int = customTcpPort
}

