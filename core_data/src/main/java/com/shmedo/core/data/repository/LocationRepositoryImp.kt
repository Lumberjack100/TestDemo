package com.shmedo.core.data.repository

import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.blankj.utilcode.util.Utils
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.extensions.getLogItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/11 <br/>
 * 描述：     位置服务仓库实现类 - 优化版本
 */
class LocationRepositoryImp(private val loggerRepositoryImp: LoggerRepositoryImp) {

    companion object {
        private const val LOCATION_CACHE_DURATION = 30_000L // 30秒缓存时间
        private const val MIN_DISTANCE_CHANGE = 10.0 // 最小距离变化（米）
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY = 2_000L // 重试延迟2秒
    }

    // 线程安全的 locationClient 访问
    private var locationClient: LocationClient? = null
    private val clientMutex = Mutex()

    private val locationListener = MyLocationListener()
    private val managerJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.Default + managerJob)

    // 定位状态管理
    private val isInitialized = AtomicBoolean(false)
    private val isLocationRequesting = AtomicBoolean(false)

    // 位置信息状态流
    private val _locationStateFlow = MutableStateFlow<BDLocation?>(null)
    val locationStateFlow: StateFlow<BDLocation?> = _locationStateFlow

    // 缓存相关
    private var cachedLocation: BDLocation? = null
    private var lastLocationTime: Long = 0L
    private var retryCount = 0

    // 错误状态流
    private val _errorStateFlow = MutableStateFlow<LocationError?>(null)
    val errorStateFlow: StateFlow<LocationError?> = _errorStateFlow

    /**
     * 延迟初始化定位客户端 - 按需初始化，避免不必要的电量消耗
     */
    private suspend fun ensureLocationClientInitialized() = clientMutex.withLock {
        if (locationClient == null && !isInitialized.get()) {
            try {
                locationClient = LocationClient(Utils.getApp().applicationContext).apply {
                    registerLocationListener(locationListener)
                    locOption = defaultOption
                }
                isInitialized.set(true)
                logAndRecord("定位客户端延迟初始化成功")
            } catch (e: Exception) {
                val error = LocationError.InitializationError("定位客户端初始化失败: ${e.message}")
                _errorStateFlow.value = error
                logAndRecord("定位客户端初始化失败: ${e.message}", Log.ERROR)
                throw e
            }
        }
    }

    /**
     * 抽象出通用的定位参数配置方法
     */
    private fun createLocationOption(
        highAccuracy: Boolean,
        interval: Int
    ): LocationClientOption {
        return LocationClientOption().apply {
            //可选，设置定位模式，可选的模式有高精度、仅设备、低功耗、模糊定位。默认高精度
            locationMode = if (highAccuracy) {
                LocationClientOption.LocationMode.Hight_Accuracy
            } else {
                LocationClientOption.LocationMode.Battery_Saving
            }
            //可选，首次定位时可以选择定位的返回是准确性优先还是速度优先，默认为速度优先
            firstLocType = LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC
            //可选，发起定位请求的间隔，int类型，单位ms；如果设置为0，则代表单次定位，即仅定位一次，默认为0  如果设置非0，需设置1000ms以上才有效
            scanSpan = interval
            //可选，设置是否使用卫星定位，默认false ； 使用高精度和仅用设备两种定位模式的，参数必须设置为true
            openGps = true
            coorType = "gcj02" //可选，设置坐标类型
            //可选，设置是否返回逆地理地址信息。默认是true
            setIsNeedAddress(true)
            //可选，设置是否需要最新版本的地址信息。默认为 true
            setNeedNewVersionRgc(true)
            //可选，设置是否单次定位。默认是false
            isOnceLocation = false
            //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
            setWifiCacheTimeOut(5 * 60 * 1000)
            //定位SDK内部是一个service，并放到了独立进程,设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
            setIgnoreKillProcess(false)
        }
    }

    /**
     * 默认的定位参数
     */
    private val defaultOption: LocationClientOption
        get() = createLocationOption(highAccuracy = true, interval = 15 * 1000)

    /**
     * 内部定位监听器
     */
    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            managerScope.launch {
                processLocationResult(bdLocation)
            }
        }
    }

    /**
     * 处理定位结果
     */
    private suspend fun processLocationResult(bdLocation: BDLocation?) {
        isLocationRequesting.set(false)

        if (bdLocation == null) {
            handleLocationError(LocationError.NullLocationError("定位结果为空"))
            return
        }

        when (bdLocation.locType) {
            BDLocation.TypeGpsLocation,
            BDLocation.TypeNetWorkLocation,
            BDLocation.TypeOffLineLocation -> {
                // 定位成功
                handleLocationSuccess(bdLocation)
                retryCount = 0 // 重置重试计数
            }

            else -> {
                // 定位失败
                val error = LocationError.LocationFailedError(
                    code = bdLocation.locType,
                    error = bdLocation.locTypeDescription ?: "未知定位错误"
                )
                handleLocationError(error)
            }
        }
    }

    /**
     * 处理定位成功
     */
    private fun handleLocationSuccess(bdLocation: BDLocation) {
        val currentTime = System.currentTimeMillis()

        // 检查是否需要更新缓存（基于时间和距离变化）
        val shouldUpdateCache = shouldUpdateLocationCache(bdLocation, currentTime)

        if (shouldUpdateCache) {
            cachedLocation = bdLocation
            lastLocationTime = currentTime
            _locationStateFlow.value = bdLocation
            _errorStateFlow.value = null // 清除错误状态

            logAndRecord(
                "定位成功: lat=${bdLocation.latitude}, lng=${bdLocation.longitude}, " +
                        "type=${bdLocation.locType}, address=${bdLocation.addrStr}"
            )
        } else {
            logAndRecord("定位结果未发生显著变化，使用缓存位置")
        }
    }

    /**
     * 判断是否应该更新位置缓存
     */
    private fun shouldUpdateLocationCache(newLocation: BDLocation, currentTime: Long): Boolean {
        val cachedLoc = cachedLocation

        // 如果没有缓存或者缓存过期，则更新
        return cachedLoc == null || currentTime - lastLocationTime > LOCATION_CACHE_DURATION

//        // 计算距离变化
//        val distance = calculateDistance(
//            cachedLoc.latitude, cachedLoc.longitude,
//            newLocation.latitude, newLocation.longitude
//        )
//        // 如果距离变化超过阈值，则更新
//        return distance > MIN_DISTANCE_CHANGE
    }

    /**
     * 计算两点之间的距离（简化版）
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val deltaLat = abs(lat1 - lat2)
        val deltaLng = abs(lng1 - lng2)
        // 简化计算，实际项目中可使用更精确的地球距离计算公式
        return (deltaLat + deltaLng) * 111_000 // 大约转换为米
    }

    /**
     * 处理定位错误
     */
    private suspend fun handleLocationError(error: LocationError) {
        _errorStateFlow.value = error
        logAndRecord("定位错误: ${error.message}", Log.ERROR)

        // 实现重试机制
        if (retryCount < MAX_RETRY_COUNT && error is LocationError.LocationFailedError) {
            retryCount++
            logAndRecord("定位失败，准备进行第 $retryCount 次重试")
            delay(RETRY_DELAY)
            requestLocationWithRetry()
        }
    }

    /**
     * 立即请求位置更新 - 优先返回缓存的有效位置
     */
    suspend fun requestImmediateLocationUpdate(): BDLocation? {
        // 首先检查缓存是否有效
        val currentTime = System.currentTimeMillis()
        cachedLocation?.let { cached ->
            if (currentTime - lastLocationTime < LOCATION_CACHE_DURATION) {
                logAndRecord("返回缓存的位置信息")
                return cached
            }
        }

        // 缓存无效或不存在，请求新的位置
        return requestLocationWithRetry()
    }

    /**
     * 带重试机制的定位请求
     */
    private suspend fun requestLocationWithRetry(): BDLocation? {
        if (isLocationRequesting.get()) {
            logAndRecord("定位请求正在进行中，跳过重复请求")
            return cachedLocation
        }

        return try {
            ensureLocationClientInitialized()
            isLocationRequesting.set(true)

            clientMutex.withLock {
                locationClient?.let { client ->
                    if (!client.isStarted) {
                        client.start()
                        logAndRecord("启动定位服务")
                    }
                    client.requestLocation()
                    logAndRecord("请求位置更新")
                }
            }

            // 等待定位结果或超时
            withTimeoutOrNull(30_000L) {
                while (isLocationRequesting.get()) {
                    delay(100)
                }
                _locationStateFlow.value
            }
        } catch (e: Exception) {
            isLocationRequesting.set(false)
            val error = LocationError.RequestError("定位请求失败: ${e.message}")
            handleLocationError(error)
            null
        }
    }


    /**
     * 停止定位服务
     */
    suspend fun stopLocation() {
        clientMutex.withLock {
            locationClient?.takeIf { it.isStarted }?.let {
                it.stop()
                logAndRecord("停止定位服务")
            }
        }
        isLocationRequesting.set(false)
    }

    /**
     * 清理资源
     */
    suspend fun clear() {
        clientMutex.withLock {
            locationClient?.let { client ->
                client.unRegisterLocationListener(locationListener)
                if (client.isStarted) {
                    client.stop()
                }
            }
            locationClient = null
        }

        isInitialized.set(false)
        isLocationRequesting.set(false)
        cachedLocation = null
        lastLocationTime = 0L
        retryCount = 0

        managerJob.cancel()
        logAndRecord("定位服务资源已清理")
    }

    /**
     * 获取缓存的位置信息
     */
    fun getCachedLocation(): BDLocation? {
        val currentTime = System.currentTimeMillis()
        return if (cachedLocation != null && currentTime - lastLocationTime < LOCATION_CACHE_DURATION) {
            cachedLocation
        } else {
            null
        }
    }

    /**
     * 检查定位服务是否可用
     */
    fun isLocationServiceAvailable(): Boolean {
        return isInitialized.get() && locationClient != null
    }

    /**
     * 统一日志记录逻辑
     */
    private fun logAndRecord(message: String, level: Int = Log.INFO) {
        Timber.i(message)
        managerScope.launch {
            loggerRepositoryImp.insertLogItem(
                getLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = level,
                    data = message
                )
            )
        }
    }
}

/**
 * 定位错误类型定义
 */
sealed class LocationError(val message: String) {
    data class InitializationError(val error: String) : LocationError("初始化错误: $error")
    data class NullLocationError(val error: String) : LocationError("空位置错误: $error")
    data class LocationFailedError(val code: Int, val error: String) :
        LocationError("定位失败[code:$code]: $error")

    data class RequestError(val error: String) : LocationError("请求错误: $error")
}