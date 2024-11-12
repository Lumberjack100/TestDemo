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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/11 <br/>
 * 描述：     TODO
 */
class LocationRepositoryImp(private val loggerRepositoryImp: LoggerRepositoryImp) {
    private var locationClient: LocationClient? = null
    private val locationListener = MyLocationListener()
    private val managerJob = SupervisorJob()
    private val managerScope = CoroutineScope(Dispatchers.Default + managerJob)

    private val _locationStateFlow = MutableStateFlow<BDLocation?>(null)
    val locationStateFlow: StateFlow<BDLocation?> = _locationStateFlow


    init {
        initializeLocationClient()
    }

    private fun initializeLocationClient() {
        locationClient = LocationClient(Utils.getApp().applicationContext).apply {
            registerLocationListener(locationListener) // Register listener before starting
            locOption = defaultOption
            start() // Start the client after registering the listener
        }
        logAndRecord("Location client initialized and started.")
    }

    //抽象出通用的定位参数配置方法
    private fun createLocationOption(
        highAccuracy: Boolean,
        interval: Int
    ): LocationClientOption {
        return LocationClientOption().apply {
            //可选，设置定位模式，可选的模式有高精度、仅设备、低功耗、模糊定位。默认高精度
            locationMode =
                if (highAccuracy) LocationClientOption.LocationMode.Hight_Accuracy else LocationClientOption.LocationMode.Battery_Saving
            //可选，首次定位时可以选择定位的返回是准确性优先还是速度优先，默认为速度优先
            firstLocType = LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC
            //可选，发起定位请求的间隔，int类型，单位ms；如果设置为0，则代表单次定位，即仅定位一次，默认为0  如果设置非0，需设置1000ms以上才有效
            scanSpan = interval
            //可选，设置是否使用卫星定位，默认false ； 使用高精度和仅用设备两种定位模式的，参数必须设置为true
            openGps = true
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

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            if (bdLocation == null) {
                val msg = "定位失败: bdLocation 对象为空"
                logAndRecord(msg, Log.ERROR)
                return
            }

            if (bdLocation.locType != BDLocation.TypeServerError) {
                Timber.i("定位成功, latitude=${bdLocation.latitude},longitude=${bdLocation.longitude},altitude=${bdLocation.altitude},coordType=${bdLocation.coorType},locationType=${bdLocation.locType},speed=${bdLocation.speed},adCode=${bdLocation.adCode}")
                _locationStateFlow.value = bdLocation
                return
            }

            val msg =
                "定位失败: 错误码=${bdLocation.locType}, 错误信息=${bdLocation.locTypeDescription}"
            logAndRecord(msg, Log.ERROR)
        }
    }

    fun requestImmediateLocationUpdate() {
        val msg = "Requesting immediate location update"
        logAndRecord(msg)
        //立即请求一个位置更新
        locationClient?.isStarted?.let {
            if (it) {
                locationClient?.requestLocation()
            } else {
                locationClient?.start()
            }
        }
    }

    fun requestDefaultLocation() {
        locationClient?.apply {
            if (isStarted) {
                stop()
            }
            locOption = defaultOption
            start()
        }
    }

    fun stopLocation() {
        locationClient?.stop()
    }

    fun clear() {
        locationClient?.unRegisterLocationListener(locationListener)
        locationClient?.stop()
        locationClient = null
        managerJob.cancel() //取消与这个作用域关联的所有协程
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