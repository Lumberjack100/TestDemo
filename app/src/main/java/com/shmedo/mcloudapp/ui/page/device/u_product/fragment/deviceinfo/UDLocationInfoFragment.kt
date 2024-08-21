package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdLocationInfoBinding
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.UDLocationInfoViewModel
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位计位置信息
 *
 */
class UDLocationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdLocationInfoBinding
    private val mStates: UDLocationInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var aMap: AMap //地图控制器对象
    private lateinit var gcjLatLng: CustomLatLng
    private var curMaker: Marker? = null
    private var mZoomLevel = 15f //地图的缩放级别一共分为 17 级，从 3 到 19。数字越大，展示的图面信息越精细。


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_location_info,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdLocationInfoBinding
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
        aMap.isMyLocationEnabled = false //可触发定位并显示当前位置
    }

    override fun initData() {
        super.initData()
//        arguments?.let {
//            centerNum = it.getInt(CENTER_NUM_PARAM, 4)
//        }
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
    }

    override fun lazyLoadData() {
        queryInfo()
    }

    private fun queryInfo() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {

            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询通讯状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {

                        }
                        val content: String = result.data
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun addMarker() {
        curMaker?.remove()
        curMaker = null

        try {
            val latlng = LatLng(gcjLatLng.latitude, gcjLatLng.longitude)
            val markerOption = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_device_location))
                .position(latlng)
                .draggable(false)

            curMaker = aMap.addMarker(markerOption)
            //设置指定的可视区域地图
            moveCameraToLocation(latlng)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel))
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
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
        fun newInstance() = UDLocationInfoFragment()
    }
}