package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.LogoPosition
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.data.repository.DeviceManageRepositoryImp
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentGt600LocationInfoBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.toGcj02LatLng
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.Gt600LocationInfoViewModel
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

/**
 * GT600 姿态监测页面
 *
 * 功能：
 * 1. 固定横屏显示
 * 2. 左侧卫星地图展示设备位置
 * 3. 右侧展示设备状态信息（基准站、移动站、倾角计状态等）
 * 4. 每秒刷新传感器数据
 * 
 * @author: AI Agent
 * @time: 2026/1/5
 */
class Gt600LocationInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentGt600LocationInfoBinding
    private val mStates: Gt600LocationInfoViewModel by viewModels()
    private val deviceManageRepository: DeviceManageRepositoryImp by inject()

    // 地图相关
    private lateinit var baiduMap: BaiduMap
    private var curMarker: Marker? = null
    private var lastLatLng: LatLng? = null // 上一次打点的经纬度
    private val mZoomLevel = 18f

    // 定时任务
    private var dataRefreshJob: Job? = null

    // 传感器ID（从 QueryDeviceSensor 获取）
    private var sensorId: String? = null

    // 设备Token（从 arguments 获取）
    private var deviceToken: String = ""

    companion object {
        // 距离阈值（米）- 超过此距离才更新地图打点
        private const val DISTANCE_THRESHOLD_METERS = 10.0
        private const val SENSOR_TYPE_6007 = "6007"
        private const val MODE_DIFFERENTIAL = 68  // D - 差分模式
        private const val MODE_AUTONOMOUS = 65    // A - 自主模式

        /**
         * 创建 Bundle 参数
         */
        fun newBundleArguments(deviceToken: String): Bundle {
            return Bundle().apply {
                putString("deviceToken", deviceToken)
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_gt600_location_info,
            BR.stateVM,
            mStates
        ).addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600LocationInfoBinding

        // 注册返回键处理
        registerOnBackPressedDispatcher {
            restoreOrientationAndNavigateUp()
        }

        // 初始化地图
        setupMap()
    }

    override fun initData() {
        // 获取设备Token
        arguments?.let {
            deviceToken = it.getString("deviceToken", "")
        }
    }

    override fun onResume() {
        super.onResume()
        // 设置固定横屏
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun lazyLoadData() {
        // 开始加载数据
        launchWithViewLifecycle {
            // 1. 先查询传感器ID
            querySensorId()

            // 2. 如果获取到传感器ID，开始定时刷新数据
            if (sensorId != null) {
                startDataRefreshTimer()
            }
        }
    }

    /**
     * 设置地图
     */
    private fun setupMap() {
        baiduMap = binding.mapView.map
        // 设置卫星地图图层
        baiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE
        baiduMap.setMaxAndMinZoomLevel(20f, 3f)
        baiduMap.setCompassEnable(false)
        binding.mapView.showZoomControls(false)
        binding.mapView.showScaleControl(false)
        binding.mapView.logoPosition = LogoPosition.logoPostionleftBottom
        baiduMap.setViewPadding(0, 0, 0, 0)

        // 默认位置（上海）
        moveCameraToLocation(LatLng(31.21032874, 121.59840681))
    }

    /**
     * 查询传感器ID
     */
    private suspend fun querySensorId() {
        try {
            val sensorList = withContext(Dispatchers.IO) {
                deviceManageRepository.queryDeviceSensorListWithPage(
                    deviceToken = deviceToken,
                    iotSensorType = SENSOR_TYPE_6007,
                    currentPage = 1,
                    pageSize = 1
                )?.currentPageData
            }

            // 获取第一个传感器的ID
            sensorId = sensorList?.firstOrNull()?.id?.toString()

            if (sensorId == null) {
                Timber.w("未找到类型为 $SENSOR_TYPE_6007 的传感器")
            } else {
                Timber.d("获取到传感器ID: $sensorId")
            }
        } catch (e: Exception) {
            Timber.e(e, "查询传感器ID失败")
        }
    }

    /**
     * 启动数据刷新定时器（每秒刷新）
     */
    private fun startDataRefreshTimer() {
        dataRefreshJob?.cancel()
        dataRefreshJob = launchWithViewLifecycle {
            while (isActive) {
                querySensorData()
                delay(1000) // 每秒刷新
            }
        }
    }

    /**
     * 查询传感器最新数据
     */
    private suspend fun querySensorData() {
        val currentSensorId = sensorId ?: return

        try {
            val dataList = withContext(Dispatchers.IO) {
                deviceManageRepository.querySensorNewData(currentSensorId)
            }

            // 获取第一条数据
            val data = dataList?.firstOrNull() ?: return

            // 更新UI
            updateUI(data)

        } catch (e: Exception) {
            Timber.e(e, "查询传感器数据失败")
        }
    }

    /**
     * 更新UI
     */
    private fun updateUI(data: Map<String, String>) {
        // 解析数据
        val mode = data["mode"]?.toIntOrNull() ?: 0
        val xAng = data["x_ang"]?.toDoubleOrNull() ?: 0.0
        val yAng = data["y_ang"]?.toDoubleOrNull() ?: 0.0
        val zAng = data["z_ang"]?.toDoubleOrNull() ?: 0.0
        val heading = data["heading"]?.toDoubleOrNull() ?: 0.0
        val lat = data["lat"]?.toDoubleOrNull() ?: 0.0
        val lon = data["lon"]?.toDoubleOrNull() ?: 0.0
        val time = data["time"] ?: ""

        // 1. 基准站状态：mode == 68 (D) 为正常
        val isBaseNormal = mode == MODE_DIFFERENTIAL
        mStates.isBaseStationNormal.set(isBaseNormal)
        mStates.baseStationStatusText.set(if (isBaseNormal) "正常" else "异常")

        // 2. 移动站状态：mode == 65 (A) 或 68 (D) 为正常
        val isRoverNormal = mode == MODE_AUTONOMOUS || mode == MODE_DIFFERENTIAL
        mStates.isRoverStationNormal.set(isRoverNormal)
        mStates.roverStationStatusText.set(if (isRoverNormal) "正常" else "异常")

        // 3. 倾角计状态：x_ang + y_ang + z_ang 绝对值之和 == 0 为正常
        val angSum = abs(xAng) + abs(yAng) + abs(zAng)
        val isInclinometerNormal = angSum == 0.0
        mStates.isInclinometerNormal.set(isInclinometerNormal)
        mStates.inclinometerStatusText.set(if (isInclinometerNormal) "正常" else "异常")

        // 4. 时间：格式化为 "2026年1月12日 09:08:16.000"
        mStates.displayTime.set(formatTime(time))

        // 5. 位置：格式化为 "北纬31.212495°,东经121.594751°"
        mStates.displayLocation.set(formatLocation(lat, lon))

        // 6. 方位角
        mStates.heading.set("${heading}°")

        // 7. 仰角 (x_ang)
        mStates.elevation.set("${xAng}°")

        // 8. 更新经纬度
        mStates.latitude.set(lat)
        mStates.longitude.set(lon)

        // 9. 更新地图打点（控制更新频率）
        updateMapMarker(lat, lon)
    }

    /**
     * 格式化时间
     * 输入: "2026-01-05 09:08:16.000"
     * 输出: "2026年1月5日 09:08:16.000"
     */
    private fun formatTime(timeStr: String): String {
        if (timeStr.isEmpty()) return "--"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy年M月d日 HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timeStr)
            date?.let { outputFormat.format(it) } ?: timeStr
        } catch (e: Exception) {
            // 如果解析失败，尝试不带毫秒的格式
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy年M月d日 HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(timeStr)
                date?.let { outputFormat.format(it) } ?: timeStr
            } catch (e2: Exception) {
                timeStr
            }
        }
    }

    /**
     * 格式化位置
     * 输出: "北纬31.212495°,东经121.594751°"
     */
    private fun formatLocation(lat: Double, lon: Double): String {
        if (lat == 0.0 && lon == 0.0) return "--"
        return "${lat}°,${lon}°"
    }

    /**
     * 更新地图打点
     * 只有当距离超过阈值时才更新
     */
    private fun updateMapMarker(lat: Double, lon: Double) {
        if (lat == 0.0 && lon == 0.0) return

        // WGS84 转 GCJ02（中国火星坐标系）
        val gcjLatLng = CustomLatLng(lat, lon).toGcj02LatLng()

        // 计算与上一次打点的距离
//        val lastPoint = lastLatLng
//        if (lastPoint != null) {
//            val distance = DistanceUtil.getDistance(lastPoint, gcjLatLng)
//            if (distance < DISTANCE_THRESHOLD_METERS) {
//                // 距离小于阈值，不更新打点
//                return
//            }
//        }

        // 更新打点
        addMarker(gcjLatLng)
        lastLatLng = gcjLatLng
    }

    /**
     * 添加地图标记
     */
    private fun addMarker(latLng: LatLng) {
        // 移除旧标记
        curMarker?.remove()
        curMarker = null

        try {
            val markerOption = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_device_location))
                .position(latLng)
                .draggable(false)

            curMarker = baiduMap.addOverlay(markerOption) as Marker
            moveCameraToLocation(latLng)
        } catch (ex: Exception) {
            Timber.e(ex, "添加地图标记失败")
        }
    }

    /**
     * 移动地图到指定位置
     */
    private fun moveCameraToLocation(latLng: LatLng) {
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, mZoomLevel))
    }

    /**
     * 恢复屏幕方向并返回
     */
    private fun restoreOrientationAndNavigateUp() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        nav().navigateUp()
    }

    /**
     * 点击事件代理
     */
    inner class ClickProxy : BaseClickProxy() {
        /**
         * 返回按钮点击
         */
        fun onBackPressed() {
            restoreOrientationAndNavigateUp()
        }

        /**
         * 回到当前位置
         */
        fun backToLocation() {
            lastLatLng?.let {
                moveCameraToLocation(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        dataRefreshJob?.cancel()
        // 恢复屏幕方向
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroy()
        binding.mapView.onDestroy()
    }
}

