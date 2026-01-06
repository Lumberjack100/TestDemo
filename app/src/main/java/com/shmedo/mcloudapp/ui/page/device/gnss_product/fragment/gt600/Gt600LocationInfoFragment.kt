package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.LogoPosition
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.toast.Toaster
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
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * GT600 人工降雨高炮姿态监测页面
 *
 * ## 功能概述
 * 1. **固定横屏显示**：页面始终保持横屏方向，退出时恢复竖屏
 * 2. **卫星地图展示**：左侧显示百度卫星地图，实时展示高射炮车位置和朝向
 * 3. **状态信息面板**：右侧显示设备状态信息，包括基准站、移动站、倾角计状态等
 * 4. **实时数据刷新**：每秒自动刷新传感器数据并更新UI
 * 5. **截图保存功能**：支持将当前页面截图保存到相册
 *
 * ## 页面布局
 * - **左侧**：卫星地图（占比 1），显示高射炮车图标，图标根据方位角实时旋转
 * - **右侧**：数据面板（固定宽度 280dp），显示设备状态和位置信息
 *
 * ## 数据流程
 * 1. 通过 `QueryDeviceSensor` 接口查询类型为 "6007" 的传感器ID
 * 2. 使用 `DescribeSensorNewData` 接口每秒获取最新传感器数据
 * 3. 解析数据并更新状态信息和地图标记
 *
 * ## 坐标转换
 * - 传感器返回的经纬度为 **WGS84** 坐标系（GPS 原始坐标）
 * - 百度地图使用 **GCJ02** 坐标系（中国火星坐标系）
 * - 使用 `CustomLatLng.toGcj02LatLng()` 进行坐标转换
 *
 * @author: AI Agent
 * @date: 2026/1/5
 */
class Gt600LocationInfoFragment : BaseFragment() {

    // ==================== 视图与数据绑定 ====================

    /**
     * DataBinding 实例，用于绑定布局文件
     */
    private lateinit var binding: FragmentGt600LocationInfoBinding

    /**
     * ViewModel 实例，存储 UI 状态数据
     * 通过 DataBinding 自动更新 UI
     */
    private val mStates: Gt600LocationInfoViewModel by viewModels()

    /**
     * 设备管理仓库，用于网络请求
     * 通过 Koin 依赖注入
     */
    private val deviceManageRepository: DeviceManageRepositoryImp by inject()

    // ==================== 地图相关 ====================

    /**
     * 百度地图实例
     * 用于地图操作、标记添加、相机移动等
     */
    private lateinit var baiduMap: BaiduMap

    /**
     * 当前地图上的标记（高射炮车图标）
     * 每次更新位置时会移除旧标记，添加新标记
     */
    private var curMarker: Marker? = null

    /**
     * 上一次打点的经纬度
     * 用于计算距离差，避免频繁更新地图标记
     */
    private var lastLatLng: LatLng? = null

    /**
     * 上一次的方位角
     * 用于判断方位角变化是否超过阈值，避免频繁更新图标旋转
     */
    private var lastHeading: Float? = null

    /**
     * 地图缩放级别
     * 18 级适合查看设备具体位置和周边环境
     */
    private val mZoomLevel = 18f

    // ==================== 定时任务 ====================

    /**
     * 数据刷新定时任务
     * 每秒执行一次，查询传感器最新数据
     * 在页面销毁时自动取消
     */
    private var dataRefreshJob: Job? = null

    // ==================== 设备信息 ====================

    /**
     * 传感器ID
     * 通过 QueryDeviceSensor 接口查询得到
     * 用于调用 DescribeSensorNewData 接口获取最新数据
     */
    private var sensorId: String? = null

    /**
     * 设备Token
     * 从 arguments 中获取，用于设备相关的网络请求
     */
    private var deviceToken: String = ""

    companion object {
        // ==================== 常量定义 ====================

        /**
         * 距离阈值（米）
         * 当设备移动距离超过此阈值时才更新地图标记
         * 避免频繁更新导致地图抖动
         *
         * 注：当前此功能已注释，每次都会更新标记
         */
        private const val DISTANCE_THRESHOLD_METERS = 10.0

        /**
         * 方位角差值阈值（度）
         * 当方位角变化超过此阈值时才更新图标旋转角度
         * 避免频繁旋转导致图标抖动
         */
        private const val HEADING_THRESHOLD_DEGREES = 5.0f

        /**
         * 传感器类型：GT600 姿态监测传感器
         * 用于查询特定类型的传感器
         */
        private const val SENSOR_TYPE_6007 = "6007"

        /**
         * 差分模式（Differential）
         * mode 字段值为 68（字符 'D' 的 ASCII 码）
         * 表示基准站和移动站均正常工作，定位精度最高
         */
        private const val MODE_DIFFERENTIAL = 68  // D - 差分模式

        /**
         * 自主模式（Autonomous）
         * mode 字段值为 65（字符 'A' 的 ASCII 码）
         * 表示仅使用移动站自身进行定位，精度较低
         */
        private const val MODE_AUTONOMOUS = 65    // A - 自主模式

        /**
         * 创建页面参数 Bundle
         *
         * @param deviceToken 设备Token，用于标识设备
         * @return Bundle 包含设备Token的参数对象
         *
         * 使用示例：
         * ```kotlin
         * val bundle = Gt600LocationInfoFragment.newBundleArguments(deviceToken)
         * findNavController().navigate(R.id.gt600LocationInfoFragment, bundle)
         * ```
         */
        fun newBundleArguments(deviceToken: String): Bundle {
            return Bundle().apply {
                putString("deviceToken", deviceToken)
            }
        }
    }

    // ==================== 生命周期方法 ====================

    /**
     * 获取 DataBinding 配置
     *
     * @return DataBindingConfig 配置对象
     *
     * 配置说明：
     * - 布局文件：fragment_gt600_location_info.xml
     * - stateVM：绑定 Gt600LocationInfoViewModel，用于数据展示
     * - click：绑定 ClickProxy，用于处理点击事件
     */
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_gt600_location_info,
            BR.stateVM,
            mStates
        ).addBindingParam(BR.click, ClickProxy())
    }

    /**
     * 初始化视图
     *
     * 功能：
     * 1. 获取 DataBinding 实例
     * 2. 注册返回键监听，处理横竖屏切换
     * 3. 初始化百度地图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600LocationInfoBinding

        // 注册返回键处理
        // 按返回键时恢复竖屏方向并返回上一页
        registerOnBackPressedDispatcher {
            restoreOrientationAndNavigateUp()
        }

        // 处理系统栏 insets，避免内容被导航栏遮挡
        setupWindowInsets()

        // 初始化地图
        setupMap()
    }

    /**
     * 设置窗口 Insets，避免内容被系统栏遮挡
     *
     * 参考 InsetsManager 的实现思路，但针对横屏场景处理所有方向的 insets：
     * - top: 状态栏（横屏时通常很小或没有）
     * - right: 导航栏（横屏时最重要，通常在右侧）
     * - bottom: 底部系统栏（横屏时通常没有）
     * - left: 左侧系统栏（某些设备可能有）
     */
    private fun setupWindowInsets() {
        // 保存初始 padding
        val initialPadding = with(binding.root) {
            intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            // 获取系统栏的 insets
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 为根布局设置 padding，避免内容被系统栏遮挡
            // 横屏模式下，right insets 最重要（导航栏通常在右侧）
            view.setPadding(
                initialPadding[0] ,    // 左侧
                initialPadding[1],     // 顶部（状态栏）
                initialPadding[2] + systemBars.right,   // 右侧（导航栏）★ 重点
                initialPadding[3]   // 底部
            )

            // 记录日志，便于调试
            Timber.d("系统栏 Insets - left:${systemBars.left}, top:${systemBars.top}, right:${systemBars.right}, bottom:${systemBars.bottom}")

            // 不消费 insets，让子视图也能接收
            windowInsets
        }

        // 请求应用 insets
        ViewCompat.requestApplyInsets(binding.root)
    }

    /**
     * 初始化数据
     *
     * 功能：
     * 从 arguments 中获取设备Token
     */
    override fun initData() {
        // 获取设备Token
        arguments?.let {
            deviceToken = it.getString("deviceToken", "")
        }
    }

    /**
     * 页面恢复时调用
     *
     * 功能：
     * 1. 设置固定横屏显示
     * 2. 恢复地图状态
     */
    override fun onResume() {
        super.onResume()
        // 设置固定横屏
        // 确保页面始终以横屏方式显示，方便查看地图和数据
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // 恢复地图
        binding.mapView.onResume()
    }

    /**
     * 页面暂停时调用
     *
     * 功能：
     * 暂停地图，释放资源
     */
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    /**
     * 懒加载数据
     * 在页面可见时才开始加载数据，避免不必要的网络请求
     *
     * 流程：
     * 1. 查询传感器ID（通过 QueryDeviceSensor 接口）
     * 2. 如果成功获取传感器ID，启动定时刷新任务
     * 3. 每秒查询一次传感器最新数据（通过 DescribeSensorNewData 接口）
     */
    override fun lazyLoadData() {
        // 开始加载数据
        launchWithViewLifecycle {
            // 1. 先查询传感器ID
            querySensorId()

            // 2. 如果获取到传感器ID，开始定时刷新数据
            if (sensorId != null) {
                startDataRefreshTimer()
            } else {
                Timber.w("无法启动数据刷新：未获取到传感器ID")
            }
        }
    }

    /**
     * 保存实例状态
     *
     * @param outState 保存状态的 Bundle
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存地图状态（缩放级别、中心位置等）
        binding.mapView.onSaveInstanceState(outState)
    }

    /**
     * 页面销毁时调用
     *
     * 功能：
     * 1. 取消定时刷新任务
     * 2. 恢复竖屏方向
     * 3. 销毁地图，释放资源
     */
    override fun onDestroy() {
        // 取消定时任务
        dataRefreshJob?.cancel()
        // 恢复屏幕方向为竖屏
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroy()
        // 销毁地图
        binding.mapView.onDestroy()
    }

    // ==================== 地图初始化与操作 ====================

    /**
     * 初始化百度地图
     *
     * 配置说明：
     * 1. **地图类型**：卫星地图（MAP_TYPE_SATELLITE），清晰展示地形和建筑
     * 2. **缩放级别**：3-20 级，支持从全国到街道的多级缩放
     * 3. **指南针**：关闭，因为地图固定正北朝上
     * 4. **旋转手势**：禁用，保持地图正北朝上，避免方向混乱
     * 5. **俯视手势**：禁用，保持地图平面视角
     * 6. **缩放控件**：隐藏，使用手势缩放
     * 7. **比例尺**：隐藏，减少UI干扰
     * 8. **Logo位置**：左下角
     *
     * 默认位置：上海市中心（31.21°N, 121.60°E）
     */
    private fun setupMap() {
        baiduMap = binding.mapView.map

        // 设置卫星地图图层
        // 卫星地图比普通地图更直观，适合展示实际地形
        baiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE

        // 设置缩放级别范围
        // 最大 20 级（最详细），最小 3 级（全国视图）
        baiduMap.setMaxAndMinZoomLevel(20f, 3f)

        // 关闭指南针
        // 因为地图固定正北朝上，不需要指南针
        baiduMap.setCompassEnable(false)

        // 获取地图UI设置
        val uiSettings = baiduMap.uiSettings

        // 禁用旋转手势
        // 保持地图正北朝上，避免用户旋转地图导致方向混乱
        // 高射炮车图标的旋转角度以正北为基准，地图旋转会影响视觉效果
        uiSettings.isRotateGesturesEnabled = false

        // 禁用俯视手势
        // 保持地图平面视角，更清晰地展示设备位置
        uiSettings.isOverlookingGesturesEnabled = false

        // 隐藏缩放控件
        // 用户可以通过双指手势缩放地图
        binding.mapView.showZoomControls(false)

        // 隐藏比例尺
        // 减少UI干扰，保持界面简洁
        binding.mapView.showScaleControl(false)

        // 设置Logo位置为左下角
        binding.mapView.logoPosition = LogoPosition.logoPostionleftBottom

        // 设置地图内边距为0
        baiduMap.setViewPadding(0, 0, 0, 0)

        // 默认位置（上海市中心）
        // 如果没有获取到设备位置，先显示默认位置
        moveCameraToLocation(LatLng(31.21032874, 121.59840681))
    }

    /**
     * 移动地图相机到指定位置
     *
     * @param latLng 目标位置的经纬度（GCJ02 坐标系）
     *
     * 说明：
     * - 使用动画方式移动相机，过渡更自然
     * - 移动到指定位置并设置缩放级别
     */
    private fun moveCameraToLocation(latLng: LatLng) {
        // 创建地图状态更新对象
        // newLatLngZoom：同时设置中心点和缩放级别
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, mZoomLevel))
    }

    /**
     * 添加地图标记（高射炮车图标）
     *
     * @param latLng 标记位置的经纬度（GCJ02 坐标系）
     * @param heading 方位角（从正北顺时针旋转的角度，单位：度）
     *
     * 功能：
     * 1. 移除旧标记（避免重复标记）
     * 2. 创建新标记，设置图标、位置、旋转角度
     * 3. 将标记添加到地图上
     * 4. 移动相机到标记位置
     *
     * 旋转角度说明：
     * - heading：传感器返回的方位角，表示从正北顺时针旋转的角度
     * - 例如：heading = 90° 表示车头朝向正东
     * - 百度地图的 rotate 方法是逆时针旋转，因此使用 -heading
     * - 例如：rotate(-90°) 表示逆时针旋转 -90°，即顺时针旋转 90°
     */
    private fun addMarker(latLng: LatLng, heading: Float) {
        // 移除旧标记
        // 避免地图上出现多个重复的标记
        curMarker?.remove()
        curMarker = null

        try {
            // 创建标记选项
            val markerOption = MarkerOptions()
                // 设置标记图标为高射炮车图标
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.anti_aircraft_gun))
                // 设置标记位置
                .position(latLng)
                // 设置标记旋转角度
                // 百度地图 rotate 是逆时针旋转，取负数实现顺时针效果
                // 例如：heading = 90°（正东），rotate(-90°) 使图标顺时针旋转 90°
                .rotate(-heading)
                // 禁止拖动标记
                .draggable(false)

            // 将标记添加到地图上
            curMarker = baiduMap.addOverlay(markerOption) as Marker

            // 移动相机到标记位置
            // 确保设备位置始终在视野中心
            moveCameraToLocation(latLng)
        } catch (ex: Exception) {
            Timber.e(ex, "添加地图标记失败")
        }
    }

    // ==================== 数据查询与更新 ====================

    /**
     * 查询传感器ID
     *
     * 流程：
     * 1. 调用 QueryDeviceSensor 接口，查询类型为 "6007" 的传感器
     * 2. 获取第一个传感器的ID
     * 3. 将传感器ID保存到成员变量 sensorId
     *
     * 说明：
     * - 使用挂起函数，在IO线程中执行网络请求
     * - 如果查询失败或没有找到传感器，会记录警告日志
     */
    private suspend fun querySensorId() {
        try {
            // 在IO线程中执行网络请求
            val sensorList = withContext(Dispatchers.IO) {
                deviceManageRepository.queryDeviceSensorListWithPage(
                    deviceToken = deviceToken,          // 设备Token
                    iotSensorType = SENSOR_TYPE_6007,   // 传感器类型：6007
                    currentPage = 1,                     // 第一页
                    pageSize = 1                         // 每页1条（只需要第一个）
                )?.currentPageData
            }

            // 获取第一个传感器的ID
            sensorId = sensorList?.firstOrNull()?.id?.toString()

            // 记录日志
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
     * 启动数据刷新定时器
     *
     * 功能：
     * 每秒查询一次传感器最新数据，实时更新UI
     *
     * 说明：
     * - 使用 Kotlin Coroutines 的 while 循环实现定时任务
     * - launchWithViewLifecycle：确保在页面可见时执行，页面销毁时自动取消
     * - 取消旧任务后再创建新任务，避免重复执行
     */
    private fun startDataRefreshTimer() {
        // 取消旧的定时任务（如果存在）
        dataRefreshJob?.cancel()

        // 创建新的定时任务
        dataRefreshJob = launchWithViewLifecycle {
            // 循环执行，直到协程被取消
            while (isActive) {
                // 查询传感器最新数据
                querySensorData()
                // 延迟1秒
                delay(1000)
            }
        }
    }

    /**
     * 查询传感器最新数据
     *
     * 流程：
     * 1. 调用 DescribeSensorNewData 接口，获取传感器最新数据
     * 2. 获取第一条数据（最新的一条）
     * 3. 调用 updateUI 更新界面
     *
     * 说明：
     * - 使用挂起函数，在IO线程中执行网络请求
     * - 如果传感器ID为空，直接返回
     */
    private suspend fun querySensorData() {
        // 获取传感器ID，如果为空则直接返回
        val currentSensorId = sensorId ?: return

        try {
            // 在IO线程中执行网络请求
            val dataList = withContext(Dispatchers.IO) {
                deviceManageRepository.querySensorNewData(currentSensorId)
            }

            // 获取第一条数据（最新的一条）
            val data = dataList?.firstOrNull() ?: return

            // 更新UI
            updateUI(data)

        } catch (e: Exception) {
            Timber.e(e, "查询传感器数据失败")
        }
    }

    /**
     * 更新UI
     *
     * @param data 传感器数据，Map 格式，包含以下字段：
     *   - mode: 定位模式（65=自主模式, 68=差分模式）
     *   - x_ang: X轴倾角（仰角），单位：度
     *   - y_ang: Y轴倾角，单位：度
     *   - z_ang: Z轴倾角，单位：度
     *   - heading: 方位角，从正北顺时针旋转的角度，单位：度
     *   - lat: 纬度（WGS84 坐标系）
     *   - lon: 经度（WGS84 坐标系）
     *   - time: 时间戳，格式："yyyy-MM-dd HH:mm:ss"
     *
     * 功能：
     * 1. 解析传感器数据
     * 2. 判断设备状态（基准站、移动站、倾角计）
     * 3. 格式化时间和位置
     * 4. 更新 ViewModel 中的状态数据
     * 5. 更新地图标记
     *
     * 状态判断规则：
     * - **基准站状态**：mode == 68 (D) 为正常，否则异常
     * - **移动站状态**：mode == 65 (A) 或 68 (D) 为正常，否则异常
     * - **倾角计状态**：x_ang + y_ang + z_ang 绝对值之和 > 0 为正常，否则异常
     */
    private fun updateUI(data: Map<String, String>) {
        // ==================== 1. 解析数据 ====================

        // 定位模式：65(A)=自主模式, 68(D)=差分模式
        val mode = data["mode"]?.toIntOrNull() ?: 0

        // X轴倾角（仰角），表示高射炮的仰角
        val xAng = data["x_ang"]?.toDoubleOrNull() ?: 0.0

        // Y轴倾角
        val yAng = data["y_ang"]?.toDoubleOrNull() ?: 0.0

        // Z轴倾角
        val zAng = data["z_ang"]?.toDoubleOrNull() ?: 0.0

        // 方位角，从正北顺时针旋转的角度
        // 例如：0°=正北, 90°=正东, 180°=正南, 270°=正西
        val heading = data["heading"]?.toDoubleOrNull() ?: 0.0

        // 纬度（WGS84 坐标系）
        val lat = data["lat"]?.toDoubleOrNull() ?: 0.0

        // 经度（WGS84 坐标系）
        val lon = data["lon"]?.toDoubleOrNull() ?: 0.0

        // 时间戳
        val time = data["time"] ?: ""

        // ==================== 2. 判断设备状态 ====================

        // 【基准站状态】
        // 判断规则：mode == 68 (D) 为正常
        // 差分模式表示基准站和移动站同时工作，定位精度最高
        val isBaseNormal = mode == MODE_DIFFERENTIAL
        mStates.isBaseStationNormal.set(isBaseNormal)
        mStates.baseStationStatusText.set(if (isBaseNormal) "正常" else "异常")

        // 【移动站状态】
        // 判断规则：mode == 65 (A) 或 68 (D) 为正常
        // 自主模式或差分模式都表示移动站正常工作
        val isRoverNormal = mode == MODE_AUTONOMOUS || mode == MODE_DIFFERENTIAL
        mStates.isRoverStationNormal.set(isRoverNormal)
        mStates.roverStationStatusText.set(if (isRoverNormal) "正常" else "异常")

        // 【倾角计状态】
        // 判断规则：x_ang + y_ang + z_ang 绝对值之和 > 0 为正常
        // 如果三个角度都为0，说明倾角计没有正常工作
        val angSum = abs(xAng) + abs(yAng) + abs(zAng)
        val isInclinometerNormal = angSum > 0.0
        mStates.isInclinometerNormal.set(isInclinometerNormal)
        mStates.inclinometerStatusText.set(if (isInclinometerNormal) "正常" else "异常")

        // ==================== 3. 格式化数据 ====================

        // 【时间】格式化为 "2026年1月12日 09:08:16"
        mStates.displayTime.set(formatTime(time))

        // 【位置】格式化为 "31.212495°N,121.594751°E"
        mStates.displayLocation.set(formatLocation(lat, lon))

        // 【方位角】显示为 "90.5°"
        mStates.heading.set("${heading}°")

        // 【仰角】显示 X轴倾角，表示高射炮的仰角
        mStates.elevation.set("${xAng}°")

        // ==================== 4. 更新原始经纬度 ====================

        // 保存原始经纬度，用于后续处理（如距离计算）
        mStates.latitude.set(lat)
        mStates.longitude.set(lon)

        // ==================== 5. 更新地图打点 ====================

        // 传入经纬度和方位角，更新地图上的高射炮车图标
        updateMapMarker(lat, lon, heading)
    }

    /**
     * 格式化时间
     *
     * @param timeStr 输入时间字符串，格式："2026-01-05 09:08:16" 或 "2026-01-05 09:08:16.000"
     * @return 格式化后的时间字符串，格式："2026年1月5日 09:08:16"
     *
     * 说明：
     * - 如果输入为空，返回 "--"
     * - 如果解析失败，返回原始字符串
     * - 会尝试两种格式：带毫秒和不带毫秒
     */
    private fun formatTime(timeStr: String): String {
        if (timeStr.isEmpty()) return "--"

        return try {
            // 尝试解析时间（不带毫秒）
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
                // 如果还是失败，返回原始字符串
                timeStr
            }
        }
    }

    /**
     * 格式化位置
     *
     * @param lat 纬度
     * @param lon 经度
     * @return 格式化后的位置字符串，格式："31.212495°N,121.594751°E"
     *
     * 说明：
     * - 如果经纬度都为0，返回 "--"
     */
    private fun formatLocation(lat: Double, lon: Double): String {
        if (lat == 0.0 && lon == 0.0) return "--"
        return "${lat}°N,${lon}°E"
    }

    /**
     * 更新地图标记
     *
     * @param lat 纬度（WGS84 坐标系）
     * @param lon 经度（WGS84 坐标系）
     * @param heading 方位角（从正北顺时针旋转的角度，单位：度）
     *
     * 功能：
     * 1. 检查经纬度是否有效
     * 2. 将 WGS84 坐标转换为 GCJ02 坐标（百度地图使用的坐标系）
     * 3. 判断方位角变化是否超过阈值（5度）
     * 4. 如果方位角变化超过阈值或首次打点，更新图标旋转角度
     * 5. 否则只更新图标位置，不更新旋转角度
     * 6. 保存当前经纬度和方位角，用于下次比较
     *
     * 坐标转换说明：
     * - WGS84：GPS 原始坐标系，国际标准
     * - GCJ02：中国火星坐标系，百度地图使用的坐标系
     * - 两者存在偏移，需要转换才能正确显示位置
     *
     * 旋转角度优化：
     * - 只有当方位角变化超过5度时才更新图标旋转
     * - 避免频繁旋转导致图标抖动
     * - 位置每次都会更新，确保图标位置准确
     *
     * 距离优化（已注释）：
     * - 计算与上一次打点的距离
     * - 如果距离小于阈值（10米），不更新打点
     * - 避免频繁更新导致地图抖动
     */
    private fun updateMapMarker(lat: Double, lon: Double, heading: Double) {
        // 检查经纬度是否有效
        // 如果都为0，说明没有定位数据
        if (lat == 0.0 && lon == 0.0) return

        // WGS84 转 GCJ02（中国火星坐标系）
        // 百度地图使用 GCJ02 坐标系，需要转换才能正确显示
        val gcjLatLng = CustomLatLng(lat, lon).toGcj02LatLng()

        // 判断是否需要更新旋转角度
        // 1. 首次打点（lastHeading 为 null）
        // 2. 方位角变化超过阈值（绝对值差值 > 5度）
        val headingFloat = heading.toFloat()
        val needUpdateRotation = lastHeading == null ||
                abs(headingFloat - lastHeading!!) > HEADING_THRESHOLD_DEGREES

        // 计算与上一次打点的距离（已注释）
        // 如果距离小于阈值，不更新打点，避免地图抖动
//        val lastPoint = lastLatLng
//        if (lastPoint != null) {
//            val distance = DistanceUtil.getDistance(lastPoint, gcjLatLng)
//            if (distance < DISTANCE_THRESHOLD_METERS) {
//                // 距离小于阈值，不更新打点
//                return
//            }
//        }

        if (needUpdateRotation) {
            // 方位角变化超过阈值，更新图标旋转角度
            // 删除旧标记并创建新标记，设置新的旋转角度
            addMarker(gcjLatLng, headingFloat)
            // 保存当前方位角，用于下次比较
            lastHeading = headingFloat
            Timber.d("更新图标旋转角度: $headingFloat°")
        } else {
            // 方位角变化未超过阈值，只更新标记位置
            curMarker?.position = gcjLatLng
            Timber.d("只更新标记位置，保持旋转角度: $lastHeading°")
        }

        // 保存当前经纬度，用于下次距离计算
        lastLatLng = gcjLatLng
    }

    // ==================== 页面导航与返回 ====================

    /**
     * 恢复屏幕方向并返回上一页
     *
     * 功能：
     * 1. 将屏幕方向恢复为竖屏
     * 2. 返回到上一个页面
     *
     * 说明：
     * - 进入本页面时强制横屏，退出时需要恢复竖屏
     * - 如果不恢复，会影响其他页面的显示
     */
    private fun restoreOrientationAndNavigateUp() {
        // 恢复竖屏方向
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // 返回上一页
        nav().navigateUp()
    }

    // ==================== 截图保存功能 ====================

    /**
     * 截取页面并保存到相册
     *
     * 功能：
     * 1. 获取根视图（整个页面）
     * 2. 将视图绘制到 Bitmap 上
     * 3. 调用 saveBitmapToGallery 保存到相册
     *
     * 说明：
     * - 使用 Canvas 将视图绘制为图片
     * - 如果截屏失败，显示错误提示
     */
    private fun captureAndSaveScreenshot() {
        try {
            // 获取根视图（整个页面）
            val rootView = binding.root

            // 创建 Bitmap，大小与根视图相同
            // ARGB_8888：每个像素4字节，支持透明度
            val bitmap = Bitmap.createBitmap(
                rootView.width,    // 宽度
                rootView.height,   // 高度
                Bitmap.Config.ARGB_8888  // 颜色格式
            )

            // 创建画布，关联到 Bitmap
            val canvas = Canvas(bitmap)

            // 将根视图绘制到画布上
            // 相当于对整个页面进行截图
            rootView.draw(canvas)

            // 保存到相册
            saveBitmapToGallery(bitmap)
        } catch (e: Exception) {
            Timber.e(e, "截屏失败")
            Toaster.show("截屏失败")
        }
    }

    /**
     * 保存 Bitmap 到相册
     *
     * @param bitmap 要保存的图片
     *
     * 功能：
     * 1. 生成文件名（包含时间戳）
     * 2. 根据系统版本选择保存方式：
     *    - Android 10 及以上：使用 MediaStore API
     *    - Android 9 及以下：直接写入文件系统
     * 3. 保存完成后显示提示信息
     *
     * 文件名格式：GT600_姿态监测_20260105_091530.png
     * 保存路径：相册/mCloudApp/
     *
     * 说明：
     * - Android 10 引入了分区存储，必须使用 MediaStore API
     * - 使用 IO 线程执行文件操作，避免阻塞主线程
     * - 保存完成后回收 Bitmap，释放内存
     */
    private fun saveBitmapToGallery(bitmap: Bitmap) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程中执行文件操作
                val savedPath = withContext(Dispatchers.IO) {
                    // 生成文件名，包含时间戳
                    val fileName = "GT600_姿态监测_${
                        SimpleDateFormat(
                            "yyyyMMdd_HHmmss",
                            Locale.getDefault()
                        ).format(Date())
                    }.png"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // ==================== Android 10 及以上 ====================
                        // 使用 MediaStore API 保存图片

                        // 创建内容值对象，设置图片信息
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)  // 文件名
                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")  // MIME 类型
                            put(
                                MediaStore.Images.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES + "/mCloudApp"
                            )  // 相对路径
                        }

                        // 将内容值插入 MediaStore，获取 URI
                        val uri = requireContext().contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        // 使用 URI 打开输出流，写入图片数据
                        uri?.let {
                            requireContext().contentResolver.openOutputStream(it)
                                ?.use { outputStream ->
                                    // 将 Bitmap 压缩为 PNG 格式，写入输出流
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                }
                            // 返回保存路径（用于显示提示）
                            "相册/mCloudApp/$fileName"
                        }
                    } else {
                        // ==================== Android 9 及以下 ====================
                        // 直接写入文件系统

                        // 获取相册目录
                        val picturesDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                        // 创建应用专属目录
                        val appDir = File(picturesDir, "mCloudApp")
                        if (!appDir.exists()) {
                            appDir.mkdirs()  // 创建目录
                        }

                        // 创建文件
                        val file = File(appDir, fileName)

                        // 打开文件输出流，写入图片数据
                        FileOutputStream(file).use { outputStream ->
                            // 将 Bitmap 压缩为 PNG 格式，写入输出流
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }

                        // 通知媒体库更新
                        // 让系统相册能够显示新保存的图片
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DATA, file.absolutePath)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        }
                        requireContext().contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )

                        // 返回文件绝对路径
                        file.absolutePath
                    }
                }

                // 回收 Bitmap，释放内存
                bitmap.recycle()

                // 显示保存结果
                if (savedPath != null) {
                    Toaster.show("截图已保存到相册")
                } else {
                    Toaster.show("保存失败")
                }
            } catch (e: Exception) {
                Timber.e(e, "保存截图失败")
                Toaster.show("保存失败: ${e.message}")
            }
        }
    }

    // ==================== 点击事件代理 ====================

    /**
     * 点击事件代理类
     *
     * 功能：
     * 处理页面上的所有点击事件，包括：
     * 1. 返回按钮点击
     * 2. 回到当前位置按钮点击
     * 3. 保存截图按钮点击
     */
    inner class ClickProxy : BaseClickProxy() {
        /**
         * 返回按钮点击
         *
         * 功能：
         * 恢复竖屏方向并返回上一页
         */
        fun onBackPressed() {
            restoreOrientationAndNavigateUp()
        }

        /**
         * 回到当前位置
         *
         * 功能：
         * 将地图相机移动到最后一次打点的位置
         *
         * 使用场景：
         * 用户手动拖动地图后，可以通过此按钮快速回到设备位置
         */
        fun backToLocation() {
            lastLatLng?.let {
                moveCameraToLocation(it)
            }
        }

        /**
         * 保存截图到相册
         *
         * 功能：
         * 1. 申请存储权限
         * 2. 权限授予后，调用 captureAndSaveScreenshot 截取页面
         *
         * 权限说明：
         * - Android 10 以下需要存储权限
         * - Android 10 及以上使用 MediaStore API，不需要存储权限
         * - 但为了兼容性，仍然申请权限
         */
        fun onSaveScreenshot() {
            // 申请存储权限
            XXPermissions.with(this@Gt600LocationInfoFragment)
                .permission(PermissionLists.getWriteExternalStoragePermission())  // 存储权限
                .interceptor(PermissionInterceptor())  // 权限拦截器
                .description(PermissionDescription())   // 权限说明
                .request(OnPermissionCallback { _, deniedList ->
                    // 权限回调
                    if (deniedList.isEmpty()) {
                        // 权限已授予，开始截图
                        captureAndSaveScreenshot()
                    }
                    // 如果权限被拒绝，XXPermissions 会自动显示提示
                })
        }
    }
}
