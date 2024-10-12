package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentCommonLocationInfoBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.toGcj02LatLng
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonLocationInfoViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位计位置信息
 *
 */
class CommonLocationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentCommonLocationInfoBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: CommonLocationInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var aMap: AMap //地图控制器对象
    private var curMaker: Marker? = null
    private var mZoomLevel = 15f //地图的缩放级别一共分为 17 级，从 3 到 19。数字越大，展示的图面信息越精细。

    private var gcjLatLng: LatLng? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）
    private var timerClockJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_common_location_info,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentCommonLocationInfoBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        toolbarViewModel.toolbarTitleText.set("位置")
        binding.textureMapView.onCreate(savedInstanceState)
        setUpMap()
    }

    private fun setUpMap() {
        //初始化地图控制器对象
        aMap = binding.textureMapView.map
        aMap.mapType = AMap.MAP_TYPE_NORMAL //设置白昼地图（即普通地图)，aMap是地图控制器对象。
        aMap.uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_LEFT //设置logo位置
        aMap.uiSettings.setLogoBottomMargin(-200) //隐藏高德logo
        aMap.maxZoomLevel = MAX_ZOOM_LEVEL //设置最大缩放级别
        aMap.minZoomLevel = MIN_ZOOM_LEVEL //设置最小缩放级别
        aMap.uiSettings.isZoomControlsEnabled = false //隐藏地图默认的缩放按钮
        aMap.uiSettings.isScaleControlsEnabled = false //控制比例尺控件是否显示
        aMap.uiSettings.isMyLocationButtonEnabled = false //显示默认的定位按钮
//        aMap.isMyLocationEnabled = true //可触发定位并显示当前位置
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 刷新位置
         */
        fun refreshLocation() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            queryInfo()
        }

        fun backLocation() {
            moveCameraToLocation(gcjLatLng ?: return)
        }
    }

    override fun initData() {
        super.initData()
        moveCameraToLocation(LatLng(31.21032874, 121.59840681))
    }

    override fun lazyLoadData() {
        startTimer()
        queryInfo()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = when (productType) {
            ProductType.U_D_1, ProductType.U_D_2 -> IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_DEVICE_STATUS,
                "value=3"
            )

            else -> IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        }
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询位置信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {}
                        initUDStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询位置信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {}
                        initM50StatusInfo(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initUDStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle
                if (stateInfo.gnssStatus != "0") {
                    mStates.longitude.set("--")
                    mStates.latitude.set("--")
                    return@launchWithViewLifecycle
                }

                if (stateInfo.longitude != IOTConstants.NULL_KEY
                    && stateInfo.latitude != IOTConstants.NULL_KEY
                ) {
                    mStates.longitude.set("${stateInfo.longitudeDirection} ${stateInfo.longitude}°")
                    mStates.latitude.set("${stateInfo.latitudeDirection} ${stateInfo.latitude}°")

                    val longitude = stateInfo.longitude.toDouble()
                    val latitude = stateInfo.latitude.toDouble()
                    gcjLatLng = CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                        addMarker(this)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initM50StatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<M50CurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                //109.709961E,31.139160N,33.0862  31.21032874, 121.59840681
                stateInfo.location.split(",".toRegex()).dropLastWhile { it.isEmpty() }.let {
                    if (it.size == 3) {
                        mStates.longitude.set("E ${it[0]}°")
                        mStates.latitude.set("N ${it[1]}°")
                        mStates.elevation.set("${it[2]} m")

                        val longitude = it[0].replace("E", "").toDoubleOrNull() ?: 121.59840681
                        val latitude = it[1].replace("N", "").toDoubleOrNull() ?: 31.21032874
                        gcjLatLng = CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                            addMarker(this)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun addMarker(latLng: LatLng) {
        curMaker?.remove()
        curMaker = null

        try {
            val markerOption = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_device_location))
                .position(latLng)
                .draggable(false)

            curMaker = aMap.addMarker(markerOption)
            //设置指定的可视区域地图
            moveCameraToLocation(latLng)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel))
    }


    private fun startTimer() {
        //启动一个新的协程作为超时Job
        timerClockJob?.cancel()
        timerClockJob = launchWithViewLifecycle {
            while (isActive) {
                delay(1000)
                mStates.utcTime.set(TimeUtils.getNowString())
            }
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
        binding.textureMapView.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        binding.textureMapView.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.textureMapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.textureMapView.onDestroy()
    }

    companion object {
        private const val MAX_ZOOM_LEVEL = 18f //高德地图最大缩放级别
        private const val MIN_ZOOM_LEVEL = 3f  //高德地图最小缩放级别
        fun newInstance() = CommonLocationInfoViewModel()
    }
}