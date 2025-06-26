package com.shmedo.mcloudapp.ui.page.device.common

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.LogoPosition
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
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
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.extensions.toGcj02LatLng
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonLocationInfoViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.MapNavigationHelper
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
 * @desc: 查询位置信息
 *
 */
class CommonLocationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentCommonLocationInfoBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: CommonLocationInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var baiduMap: BaiduMap //地图控制器对象
    private var curMaker: Marker? = null
    private var mZoomLevel = 15f //地图的缩放级别一共分为 17 级，从 3 到 19。数字越大，展示的图面信息越精细。

    private var gcjLatLng: LatLng? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）
    private var timerClockJob: Job? = null

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数
    private var measureLoadingDialogId = ""


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
        binding.llToolbar.toolbar.title = "位置信息"
        setUpMap()
    }

    private fun setUpMap() {
        //初始化地图控制器对象
        baiduMap = binding.mapView.map
        baiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL //普通地图（包含3D地图）
        baiduMap.setMaxAndMinZoomLevel(MAX_ZOOM_LEVEL, MIN_ZOOM_LEVEL)
        baiduMap.setCompassEnable(false) //设置指南针是否显示
        binding.mapView.showZoomControls(false) //设置缩放按钮是否显示
        binding.mapView.showScaleControl(false)//设置比例尺控件是否显示
        binding.mapView.logoPosition = LogoPosition.logoPostionleftBottom //设置logo位置
        //地图Logo不允许遮挡，可通过以下方法可以设置地图边界区域，来避免UI遮挡
        baiduMap.setViewPadding(
            0,
            0,
            0,
            0
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 刷新位置
         */
        fun updateLocation() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

            when (productType) {
                ProductType.U_D_1, ProductType.U_D_2 -> measureLocation("0")
                else -> queryInfo()
            }
        }

        fun backLocation() {
            moveCameraToLocation(gcjLatLng ?: return)
        }
    }

    /**
     * 测量位置
     */
    private fun measureLocation(method: String) {
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_INSTALL_LOCATION, "method=$method")
        commandItems.add(command)

        if (method == "1") {
            measureLoadingDialogId =
                showLoadingWithUUID(StringUtils.getString(R.string.processing)) {
                    clearQueryMeasureResultTimeoutJob()
                    cancelNearbyCommunicationTimeoutJob()
                }
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
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
                "method=3"
            )

            ProductType.GNSS_M_1, ProductType.GNSS_M_2, ProductType.GNSS_M_5 -> IOTCommandUtil.getCommand(
                IOTCommandType.QUERY_DEVICE_STATUS
            )

            else -> IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        }
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS,
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            IOTCommandType.MD_GET_INSTALL_LOCATION -> {
                dismissLoadingDialog(measureLoadingDialogId)
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "位置更新指令下发出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS,
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            IOTCommandType.MD_GET_INSTALL_LOCATION -> {
                dismissLoadingDialog(measureLoadingDialogId)
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS,
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
                    errMsg = errMsg
                )
            }

            IOTCommandType.MD_GET_INSTALL_LOCATION -> {
                dismissLoadingDialog(measureLoadingDialogId)
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询位置信息出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
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
                        val errMsg = "查询位置信息出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {}

                        when (productType) {
                            ProductType.GNSS_M_5 -> initM50StatusInfo(result.data)
                            ProductType.LB20S -> initLB20StatusInfo(result.data)
                            else -> initCommonStatusInfo(result.data)
                        }
                    }
                }
            }

            IOTCommandType.MD_GET_INSTALL_LOCATION -> {//位置更新
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_GET_INSTALL_LOCATION
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "位置更新出错: ${result.message}"
                        dismissLoadingDialog(measureLoadingDialogId)
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        processUpdateLocationResponse(result.data)
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

                    var longitude = stateInfo.longitude.toDoubleOrNull() ?: 121.59840681
                    var latitude = stateInfo.latitude.toDoubleOrNull() ?: 31.21032874
                    if (longitude < 1) longitude = 121.59840681
                    if (latitude < 1) latitude = 31.21032874

                    gcjLatLng = CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                        addMarker(this)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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
                    if (it.size >= 3) {
                        mStates.longitude.set("E ${it[0]}°")
                        mStates.latitude.set("N ${it[1]}°")
                        mStates.elevation.set("${it[2]} m")

                        var longitude = it[0].replace("E", "").toDoubleOrNull() ?: 121.59840681
                        var latitude = it[1].replace("N", "").toDoubleOrNull() ?: 31.21032874
                        if (longitude < 1) longitude = 121.59840681
                        if (latitude < 1) latitude = 31.21032874

                        gcjLatLng = CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                            addMarker(this)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initLB20StatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content)
                }
                if (dataMap.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }
                val stateInfo = dataMap["000_1"]
                if (stateInfo == null) {
                    return@launchWithViewLifecycle
                }
                stateInfo.location.split(",".toRegex()).dropLastWhile { it.isEmpty() }.let {
                    if (it.size >= 2) {
                        mStates.longitude.set("E ${it[0].replace("E", "")}°")
                        mStates.latitude.set("N ${it[1].replace("N", "")}°")

                        var longitude =
                            it[0].replace("E", "").toDoubleOrNull() ?: 121.59840681
                        var latitude =
                            it[1].replace("N", "").toDoubleOrNull() ?: 31.21032874
                        if (longitude < 1) longitude = 121.59840681
                        if (latitude < 1) latitude = 31.21032874

                        gcjLatLng =
                            CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                                addMarker(this)
                            }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initCommonStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val resultMap =
                    MoshiUtil.fromJson<Map<String, Any>>(content) ?: return@launchWithViewLifecycle

                if (resultMap.containsKey("location")) {
                    resultMap["location"].toString().split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.let {
                            if (it.size >= 2) {
                                mStates.longitude.set("E ${it[0].replace("E", "")}°")
                                mStates.latitude.set("N ${it[1].replace("N", "")}°")

                                var longitude =
                                    it[0].replace("E", "").toDoubleOrNull() ?: 121.59840681
                                var latitude =
                                    it[1].replace("N", "").toDoubleOrNull() ?: 31.21032874
                                if (longitude < 1) longitude = 121.59840681
                                if (latitude < 1) latitude = 31.21032874

                                gcjLatLng =
                                    CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                                        addMarker(this)
                                    }
                            }
                        }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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
                .animateType(MarkerAnimateType.jump)
                .draggable(false)

            //在地图上添加Marker，并显示
            curMaker = baiduMap.addOverlay(markerOption) as Marker

            //设置指定的可视区域地图
            moveCameraToLocation(latLng)

            initInfoWindow(latLng)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, mZoomLevel))
    }

    private fun initInfoWindow(latLng: LatLng) {
        val button = Button(requireContext())
        button.setBackgroundResource(R.drawable.bubble_sel_bg)//bubble_sel_bg   map_info_window
        button.text = "去这里"
        button.setTextColor(Color.WHITE)
        button.setPadding(0, 0, 0, 5)
        button.textSize = 12f

        val infoWindow = InfoWindow(
            button,
            latLng,
            -65
        )
        infoWindow.view.setOnClickListener {
            // 获取设备名称作为目的地名称
            val destinationName = deviceInfo.deviceToken
            // 显示地图应用选择器并导航
            MapNavigationHelper.showMapAppSelector(
                requireContext(),
                latLng,
                destinationName
            )
        }
        baiduMap.showInfoWindow(infoWindow)
    }

    private fun startTimer() {
        //启动一个新的协程作为超时Job
        timerClockJob?.cancel()
        timerClockJob = launchWithViewLifecycle {
            while (isActive) {
                delay(1000)
                mStates.utcTime.set(
                    TimeUtils.millis2String(
                        TimeUtils.getNowMills() - 8 * 3600 * 1000,
                        "yyyy.MM.dd HH:mm:ss"
                    )
                )
            }
        }
    }

    /**
     * 处理
     */
    private fun processUpdateLocationResponse(resultMap: Map<String, String>) {
        try {
            resultMap["method"]?.let { code ->
                when (code) {
                    "1" -> {
                        clearQueryMeasureResultTimeoutJob()
                        startQueryMeasureResultJob()
                    }

                    "0" -> {
                        //已经有数据
                        if (resultMap.containsKey("lng_dir")
                            && resultMap.containsKey("lng")
                            && resultMap.containsKey("lat_dir")
                            && resultMap.containsKey("lat")
                        ) {
                            dismissLoadingDialog(measureLoadingDialogId)
                            cancelNearbyCommunicationTimeoutJob()
                            showMessageDialog("位置更新成功")

                            val longitudeDirection = resultMap["lng_dir"] ?: ""
                            val longitudeStr = resultMap["lng"] ?: ""
                            val latitudeDirection = resultMap["lat_dir"] ?: ""
                            val latitudeStr = resultMap["lat"] ?: ""

                            mStates.longitude.set("$longitudeDirection $longitudeStr°")
                            mStates.latitude.set("$latitudeDirection $latitudeStr°")

                            val longitude = longitudeStr.toDoubleOrNull() ?: 121.59840681
                            val latitude = latitudeStr.toDoubleOrNull() ?: 31.21032874
                            gcjLatLng =
                                CustomLatLng(latitude, longitude).toGcj02LatLng().apply {
                                    addMarker(this)
                                }
                            return
                        }

                        cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                        startQueryMeasureResultJob()
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startQueryMeasureResultJob() {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                cancelNearbyCommunicationTimeoutJob()
                showMessageDialog("位置更新失败，请稍后重试")
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            withContext(Dispatchers.Main) {
                Timber.d("查询位置更新结果轮询次数：$repeatPollNum")
            }
            measureLocation("0")
        }
    }

    private fun clearQueryMeasureResultTimeoutJob() {
        repeatPollNum = 0
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = null
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
        binding.mapView.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
    }

    companion object {
        private const val MAX_ZOOM_LEVEL = 18f //高德地图最大缩放级别
        private const val MIN_ZOOM_LEVEL = 3f  //高德地图最小缩放级别
        const val REPEAT_POLL_NUM = 10
    }
}