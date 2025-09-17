package com.shmedo.mcloudapp.startup

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import androidx.startup.Initializer
import com.shmedo.mcloudapp.utils.network.NetState
import com.shmedo.mcloudapp.utils.network.NetworkStateManager

/**
 * 创建者：gonghe
 * 创建时间：2025/9/17
 * 描述： TODO
 */
class NetworkInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val cm = context.getSystemService<ConnectivityManager>() ?: return

        // 是否忽略第一次回调
        var skipFirst = true

        // 将“判重 + 首次不通知”的公共逻辑提炼出来
        fun pushIfChanged(connected: Boolean) {
            if (skipFirst) {
                skipFirst = false
                return
            }

            NetworkStateManager.updateIfChanged(NetState(isSuccess = connected))
        }

        // 计算当前是否“真正联网”
        fun currentConnected(): Boolean {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            // 更接近你原先 NetworkUtils.isConnected() 的语义：
            // 既要具备可上网能力(INTERNET)，也应通过探测(VALIDATED)，并且有实际传输
            val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            return hasTransport && hasInternet && validated
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // 网络可用 -> 尝试上报“有网”
                pushIfChanged(true)
            }

            override fun onLost(network: Network) {
                // 丢失某条网络 -> 最稳妥是重新评估“当前是否仍有可用网络”
                pushIfChanged(currentConnected())
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                // 某些机型/ROM 常见：只回调能力变化，不回调 available/lost
                // 因此这里也做一次复算，保证状态同步
                val connected = currentConnected()
                pushIfChanged(connected)
            }
        }

        // 默认注册（系统自动追踪活跃网络，简单且省电）
        cm.registerDefaultNetworkCallback(callback)

        // 可选：如果你希望在 Debug/诊断时能反注册，可把 callback 存起来
        // AppStartupObjects.networkCallback = callback
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}