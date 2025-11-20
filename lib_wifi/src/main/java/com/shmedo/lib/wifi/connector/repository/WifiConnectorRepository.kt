package com.shmedo.lib.wifi.connector.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import com.shmedo.lib.wifi.connector.model.WifiConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/**
 * WiFi 连接仓库
 * 
 * 创建者: gonghe
 * 创建时间: 2024/12/15
 * 描述: Android 10+ 使用新的连接 API (WifiNetworkSpecifier)
 */
@RequiresApi(Build.VERSION_CODES.Q)
class WifiConnectorRepository(private val context: Context) {
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    // 保存当前的网络回调引用，用于断开连接
    private var currentNetworkCallback: ConnectivityManager.NetworkCallback? = null
    
    /**
     * 连接到指定 WiFi 网络
     * 
     * @param ssid 网络 SSID
     * @param password 密码（开放网络传 null）
     * @param isOpen 是否为开放网络
     * @return Flow<WifiConnectionState> 连接状态流
     */
    fun connectToWifi(
        ssid: String,
        password: String?,
        isOpen: Boolean = false
    ): Flow<WifiConnectionState> = callbackFlow {
        
        // 先断开之前的连接
//        disconnect()
        
        trySend(WifiConnectionState.Connecting)
        Timber.i("正在连接 WiFi: $ssid")
        
        // 构建网络规格
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .apply {
                if (!isOpen && !password.isNullOrBlank()) {
                    setWpa2Passphrase(password)
                }
            }
            .build()
        
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
//                Timber.i("WiFi 网络可用: $ssid")
                
                // 绑定进程到该网络（重要：后续的网络请求会使用这个网络）
                connectivityManager.bindProcessToNetwork(network)
                
                // 获取 IP 地址
                val linkProperties = connectivityManager.getLinkProperties(network)
                val ipAddress = linkProperties?.linkAddresses?.firstOrNull()?.address?.hostAddress ?: "未知"
                
                Timber.i("已连接到 WiFi: $ssid, IP: $ipAddress")
                trySend(WifiConnectionState.Connected(ssid, ipAddress))
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                Timber.w("WiFi 连接不可用: $ssid")
                trySend(WifiConnectionState.Error("连接超时或网络不可用"))
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Timber.w("WiFi 连接丢失: $ssid")
                trySend(WifiConnectionState.Disconnected)
                
                // 解除网络绑定
                connectivityManager.bindProcessToNetwork(null)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
//                Timber.d("WiFi 网络能力变化: $ssid")
            }
        }
        
        // 保存回调引用
        currentNetworkCallback = networkCallback
        
        try {
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        } catch (e: SecurityException) {
            Timber.e(e, "连接 WiFi 失败：权限不足")
            trySend(WifiConnectionState.Error("缺少必要权限"))
            currentNetworkCallback = null
        } catch (e: Exception) {
            Timber.e(e, "连接 WiFi 失败")
            trySend(WifiConnectionState.Error("连接失败: ${e.message}"))
            currentNetworkCallback = null
        }
        
        awaitClose {
            Timber.i("Flow 关闭，断开 WiFi 连接: $ssid")
            disconnect()
        }
    }
    
    /**
     * 断开当前 WiFi 连接
     */
    fun disconnect() {
        Timber.i("断开 WiFi 连接")
        
        currentNetworkCallback?.let { callback ->
            try {
                // 注销网络回调，这会真正断开 WiFi 连接
                connectivityManager.unregisterNetworkCallback(callback)
                Timber.i("已注销网络回调")
            } catch (e: IllegalArgumentException) {
                // 回调可能已经被注销
                Timber.w("网络回调已被注销或不存在")
            }
            currentNetworkCallback = null
        }
        
        // 解除进程网络绑定
        connectivityManager.bindProcessToNetwork(null)
    }
}

