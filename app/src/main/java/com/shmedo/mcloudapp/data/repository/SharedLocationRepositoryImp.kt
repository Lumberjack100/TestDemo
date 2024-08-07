package com.shmedo.mcloudapp.data.repository

import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.blankj.utilcode.util.Utils
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.core.data.repository.LoggerRepositoryImp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/11 <br/>
 * 描述：     TODO
 */
class SharedLocationRepositoryImp(private val loggerRepositoryImp: LoggerRepositoryImp) {
    private var locationClient: AMapLocationClient? = null
    private val managerJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.IO + managerJob)


    //抽象出通用的定位参数配置方法
    private fun createLocationOption(
        highAccuracy: Boolean,
        interval: Long
    ): AMapLocationClientOption {
        return AMapLocationClientOption().apply {
            //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            locationMode =
                if (highAccuracy) AMapLocationClientOption.AMapLocationMode.Hight_Accuracy else AMapLocationClientOption.AMapLocationMode.Battery_Saving
            //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
            isGpsFirst = highAccuracy
            //可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            httpTimeOut = 30000
            //可选，设置定位间隔。默认为2秒
            this.interval = interval
            //可选，设置是否返回逆地理地址信息。默认是true
            isNeedAddress = false
            //可选，设置是否单次定位。默认是false
            isOnceLocation = false
            //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
            isOnceLocationLatest = false
            //可选，设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP)
            //可选，设置是否使用传感器。默认是false
            isSensorEnable = true
            //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
            isWifiScan = true
            //可选，设置是否使用缓存定位，默认为true
            isLocationCacheEnable = true
        }
    }

    /**
     * 默认的定位参数
     */
    private val defaultOption: AMapLocationClientOption
        get() = createLocationOption(highAccuracy = true, interval = 2000)

    fun getLocationFlow() = callbackFlow<AMapLocation> {
        val locationListener = AMapLocationListener { aMapLocation ->
            aMapLocation ?: run {
                val msg = "定位失败: aMapLocation 对象为空"
                logAndRecord(msg)
                return@AMapLocationListener
            }

            if (aMapLocation.errorCode != 0) {
                val msg =
                    "定位失败: 错误码=${aMapLocation.errorCode}, 错误信息=${aMapLocation.errorInfo}, 错误描述=${aMapLocation.locationDetail}"
                logAndRecord(msg)
                return@AMapLocationListener
            }
            Timber.i("定位成功, latitude=${aMapLocation.latitude},longitude=${aMapLocation.longitude},altitude=${aMapLocation.altitude},coordType=${aMapLocation.coordType},locationType=${aMapLocation.locationType},accuracy=${aMapLocation.accuracy},speed=${aMapLocation.speed}")
            // Send the new location to the Flow observers
            trySend(aMapLocation).onFailure { throwable ->
                val msg = "发送定位数据失败: ${throwable?.localizedMessage ?: "Unknown error"}"
                logAndRecord(msg)
            }
        }

        // 设置定位监听
        if (locationClient == null) {
            locationClient = AMapLocationClient(Utils.getApp().applicationContext).apply {
                setLocationOption(defaultOption)
                setLocationListener(locationListener)
            }
        }
        val msg = "callbackFlow Starting location updates because the first collector has started"
        logAndRecord(msg)
        locationClient?.startLocation()

        awaitClose {
            logAndRecord("callbackFlow Stopping location updates because the last collector has finished")
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null
        }
    }

    fun requestImmediateLocationUpdate() {
        val msg = "callbackFlow requestImmediateLocationUpdate"
        logAndRecord(msg)
        // 使用高德 SDK 的方法立即请求一个位置更新
        locationClient?.startLocation()
    }

    fun stopLocation() {
        locationClient?.stopLocation()
    }

    fun clear() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
//        managerJob.cancel() //取消与这个作用域关联的所有协程
    }

    // 统一日志记录逻辑
    private fun logAndRecord(message: String, level: Int = Log.INFO) {
        Timber.i(message)
        managerScope.launch {
            loggerRepositoryImp.insertLog(
                getLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = level,
                    data = message
                )
            )
        }
    }
}