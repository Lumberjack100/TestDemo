package com.shmedo.lib.wifi.scanner.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.shmedo.lib.wifi.scanner.model.DiscoveredWifiNetwork
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber

/**
 * 创建者: gonghe
 * 创建时间: 2025/11/17
 *
 * 描述: WiFi 扫描仓库，负责 WiFi 扫描逻辑，参考 BLE 的 ScannerRepository
 * 使用 SharedFlow 触发器支持手动触发扫描
 *
 * Android 10+ 限制：
 * 1. App 只能在前台时扫描
 * 2. 扫描频率限制：120 秒内最多 4 次
 * 3. 需要定位权限（ACCESS_FINE_LOCATION）
 * 4. 需要定位服务开启
 */
class WifiScannerRepository internal constructor(
    private val context: Context,
    private val wifiDataStore: WifiDataStore
) {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    /**
     * 扫描触发器
     * 
     * 使用 MutableSharedFlow 实现手动触发扫描的机制
     * replay = 1 确保新的订阅者也能立即收到最近的触发事件
     */
    private val scanTrigger = MutableSharedFlow<Unit>(replay = 1)

    /**
     * 扫描 WiFi 网络
     *
     * 返回一个 Flow，响应扫描触发器并发送扫描状态
     * 每次触发器发出信号时，都会启动一次新的扫描
     */
    fun scanWifiNetworks(): Flow<WifiScanningState> = scanTrigger.flatMapLatest {
        callbackFlow {
            val wifiScanReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

                    if (success) {
                        processScanResults()
                    }
                    else {
                        Timber.w("WiFi 扫描失败")
//                        trySend(WifiScanningState.Error("扫描失败，请稍后重试"))
                    }
                }

                private fun processScanResults() {
                    try {
                        @Suppress("DEPRECATION")
                        val scanResults = wifiManager.scanResults

                        if (scanResults.isNullOrEmpty()) {
                            Timber.d("未发现任何 WiFi 网络")
                            trySend(WifiScanningState.NetworksDiscovered(emptyList()))
                            return
                        }

                        Timber.d("扫描到 ${scanResults.size} 个 WiFi 网络")

                        scanResults.forEach { result ->
                            val ssid = result.SSID.removePrefix("\"").removeSuffix("\"")

                            // 忽略隐藏网络
                            if (ssid.isBlank()) return@forEach

                            val network = DiscoveredWifiNetwork(
                                ssid = ssid,
                                bssid = result.BSSID,
                                capabilities = result.capabilities,
                                level = result.level,
                                frequency = result.frequency,
                                timestamp = result.timestamp,
                                scanResult = result
                            )

                            wifiDataStore.addOrUpdateNetwork(network)
                        }

                        trySend(WifiScanningState.NetworksDiscovered(wifiDataStore.networks))

                    } catch (e: SecurityException) {
                        Timber.e(e, "获取扫描结果失败：权限不足")
                        trySend(WifiScanningState.Error("缺少必要权限"))
                    } catch (e: Exception) {
                        Timber.e(e, "处理扫描结果失败")
                        trySend(WifiScanningState.Error("扫描异常: ${e.message}"))
                    }
                }
            }

            // 注册广播接收器
            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)

            Timber.i("开始扫描 WiFi 网络")
            trySend(WifiScanningState.Scanning)

            // 启动扫描
            try {
                val success = wifiManager.startScan()
                if (!success) {
                    Timber.w("启动扫描失败（可能受频率限制）")
                    // Android 10+ 有扫描频率限制，延迟后重试
                    delay(2000)
                    wifiManager.startScan()
                }
            } catch (e: SecurityException) {
                Timber.e(e, "启动扫描失败：权限不足")
                trySend(WifiScanningState.Error("缺少必要权限"))
            } catch (e: Exception) {
                Timber.e(e, "启动扫描失败")
                trySend(WifiScanningState.Error("扫描异常: ${e.message}"))
            }

            awaitClose {
                Timber.i("停止扫描 WiFi 网络")
                context.unregisterReceiver(wifiScanReceiver)
            }
        }
    }

    /**
     * 触发扫描
     * 
     * 发送触发信号，启动一次新的 WiFi 扫描
     * 此方法可以多次调用，每次调用都会启动新的扫描
     */
    fun startScan() {
        Timber.i("触发 WiFi 扫描")
        scanTrigger.tryEmit(Unit)
    }

    /**
     * 清空数据存储
     */
    fun clear() {
        wifiDataStore.clear()
    }
}

