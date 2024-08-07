package com.shmedo.mcloudapp.ui.page.device.common

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.location.AMapLocation
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdvancedSettingBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.AdvancedSettingItem
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.dialog.SyncInstallationLocationPopupView
import com.shmedo.mcloudapp.ui.viewmodel.request.LocationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.AdvancedSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/17
 * 描述： TODO
 */
open class BaseAdvancedSettingFragment : BaseIOTDeviceFragment(),
    GeocodeSearch.OnGeocodeSearchListener {
    protected lateinit var binding: FragmentAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdvancedSettingViewModel
    private val locationViewModel: LocationViewModel by activityViewModel()
    private val iotParseManager: IOTParserManager by inject()


    private var gcjLatLng: AMapLocation? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）
    private var geocoderSearch: GeocodeSearch? = null

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_advanced_setting,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdvancedSettingBinding
        binding.llToolbar.toolbar.title = "高级设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            //mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            //mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initAdapter()
        initGeocodeSearch()
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(8f), ColorUtils.getColor(
                        R.color.transparent
                    )
                )
            )
            addType<AdvancedSettingItem>(R.layout.item_advanced_setting)
            R.id.item.onClick {
                val item = getModel<AdvancedSettingItem>()
                processItemClick(item)
            }
        }
    }

    override fun initData() {
        super.initData()
        initAdapterData()
        if (isNeedSyncLocation())
            checkPermissions()
    }

    private fun initAdapterData() {
        val moduleList = mutableListOf<AdvancedSettingItem>()

        if (isNeedSyncLocation()) {
            moduleList.add(
                AdvancedSettingItem(
                    "同步安装位置",
                    AdvancedSettingItem.Type.SYNC_INSTALL_POSITION,
                )
            )
        }
        if (isNeedOffsetInitialization()) {
            moduleList.add(
                AdvancedSettingItem(
                    "告警偏移初始化",
                    AdvancedSettingItem.Type.OFFSET_INITIALIZATION,
                )
            )
        }
        if (productType != ProductType.COLLECTOR_G_0) {
            moduleList.add(
                AdvancedSettingItem(
                    "恢复出厂设置",
                    AdvancedSettingItem.Type.RESET,
                )
            )
        }
        if (communicateWay is NetPlatformConnect) {
            moduleList.add(
                AdvancedSettingItem(
                    "固件升级",
                    AdvancedSettingItem.Type.FIRMWARE,
                )
            )
        }

        if (communicateWay is BleConnect) {
            moduleList.add(
                AdvancedSettingItem(
                    "远程调试",
                    AdvancedSettingItem.Type.REMOTE_DEBUG,
                )
            )
        }
        binding.recyclerview.models = moduleList
    }

    private fun isNeedSyncLocation(): Boolean {
        return communicateWay is BleConnect && (productType == ProductType.LR200
                || productType == ProductType.LB20S || productType == ProductType.U_D_1
                || productType == ProductType.U_D_2 || productType == ProductType.U_R_1 || productType == ProductType.U_I_1)
    }

    /**
     * 是否需要初始化偏移量
     */
    private fun isNeedOffsetInitialization(): Boolean {
        return productType == ProductType.M20
                || productType == ProductType.GNSS_M_1
                || productType == ProductType.GNSS_M_2
    }

    override fun createObserver() {
        super.createObserver()
        //观察定位信息
        launchAndRepeatWithViewLifecycle(minActiveState = Lifecycle.State.STARTED) {
            locationViewModel.locationState.collectLatest { aMapLocation ->
                if (gcjLatLng == null || gcjLatLng!!.latitude == 0.0 || gcjLatLng!!.longitude == 0.0) {
                    gcjLatLng = aMapLocation
                    //将高德坐标(即GCJ-02火星坐标)转换为WGS-84世界标准地理坐标
                    val mWgsLatLng = JZLocationConverter.gcj02ToWgs84(
                        CustomLatLng(
                            aMapLocation.latitude,
                            aMapLocation.longitude
                        )
                    )
                    mStates.location.set(
                        Html.fromHtml(
                            String.format(
                                Locale.getDefault(),
                                "%.8f,%.8f",
                                mWgsLatLng.longitude,
                                mWgsLatLng.latitude
                            )
                        ).toString()
                    )
                    mStates.latitude.set(mWgsLatLng.latitude.toString())
                    mStates.longitude.set(mWgsLatLng.longitude.toString())

                    val latLonPoint = LatLonPoint(gcjLatLng!!.latitude, gcjLatLng!!.longitude)
                    searchAddressByLatLng(latLonPoint)
                }
            }
        }
    }

    protected open fun processItemClick(item: AdvancedSettingItem) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (item.type) {
            AdvancedSettingItem.Type.RESET -> {
                showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                    restoreFactory()
                }, "取消")
            }

            AdvancedSettingItem.Type.FIRMWARE -> {
                val bundle = newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(R.id.action_global_to_firmwareUpgradeFragment, bundle)
            }

            AdvancedSettingItem.Type.SYNC_INSTALL_POSITION -> {
                showSyncInstallationLocationPopup()
            }

            AdvancedSettingItem.Type.OFFSET_INITIALIZATION -> {
                showMessage("确定进行告警偏移初始化吗？", "温馨提示", "确定", {
                    offsetInitialization()
                }, "取消")
            }

            AdvancedSettingItem.Type.REMOTE_DEBUG -> {
                val bundle = newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(R.id.action_global_to_remoteDebugFragment, bundle)
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.RESET -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            StringUtils.getString(R.string.reset_failed) + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_INSTALL_LOCATION -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "同步安装位置出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("同步安装位置成功")
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "初始化失败: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始化完成")
                        }
                    }
                }
            }
            IOTCommandType.MD_RAW -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "同步安装位置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("同步安装位置成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun offsetInitialization() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            "method=1&type=gnss"
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 显示同步安装位置弹窗
     */
    private fun showSyncInstallationLocationPopup() {
        val popupView = SyncInstallationLocationPopupView(requireContext())
        popupView.setTitle("同步安装位置", mStates)
            .setClickListener(object : SyncInstallationLocationPopupView.OnClickListener {
                override fun onRefreshingLocationClick() {
                    gcjLatLng = null
                    locationViewModel.requestImmediateLocationUpdate()
                    mStates.isRefreshingLocation.set(true)
                }

                override fun onConfirmClick() {
                    locationViewModel.stopLocation()
                    setInstallationLocation()
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()

        gcjLatLng = null
        locationViewModel.requestImmediateLocationUpdate()
        mStates.isRefreshingLocation.set(true)
    }

    private fun setInstallationLocation() {
        commandItems.clear()
        val command =
            if (productType == ProductType.U_R_1 || productType == ProductType.U_I_1)
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_RAW, "content=##9161${mStates.location.get()}"
                )
            else IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_INSTALL_LOCATION,
                "lat=${mStates.latitude.get()}&lng=${mStates.longitude.get()}"
            )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initGeocodeSearch() {
        geocoderSearch = GeocodeSearch(activity)
        geocoderSearch?.setOnGeocodeSearchListener(this)
    }

    private fun searchAddressByLatLng(latLonPoint: LatLonPoint) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        val query = RegeocodeQuery(latLonPoint, 50f, GeocodeSearch.AMAP)
        geocoderSearch?.getFromLocationAsyn(query)
    }

    override fun onRegeocodeSearched(result: RegeocodeResult?, errorCode: Int) {
        mStates.isRefreshingLocation.set(false)
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
//            Toaster.show(MapErrorUtil.getErrorMsg(errorCode));
            return
        }
        result?.regeocodeAddress?.formatAddress?.let {
            mStates.address.set(it)
        }
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    //<editor-fold desc="权限申请">
    /**
     * 检查是否打开系统位置服务，如果开启了，接着检查是否授予 APP 定位权限
     */
    private fun checkPermissions() {
        if (PermissionHelper.isLocationEnabled()) {
            checkPermissionForLocation()
        } else {
            PermissionHelper.showGPSSettingDialog(mActivity, locationSettingLauncher)
        }
    }

    private val locationSettingLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult? ->
//        TODO 打开位置开关后只返回 RESULT_CANCELED，不知什么原因
//        if (result.getResultCode() == Activity.RESULT_OK) {
//            checkPermissionForLocation();
//        }
        checkPermissionForLocation()
    }

    private fun checkPermissionForLocation() {
        XXPermissions.with(this)
            // 申请多个权限
            .permission(PermissionHelper.foregroundLocationPermissions)
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {

                override fun onGranted(
                    grantedPermissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    if (!allGranted) {
                        return
                    }
                    locationViewModel.requestImmediateLocationUpdate()
                }
            })
    }
    // </editor-fold>

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocation()
        geocoderSearch?.setOnGeocodeSearchListener(null)
        geocoderSearch = null
    }
}