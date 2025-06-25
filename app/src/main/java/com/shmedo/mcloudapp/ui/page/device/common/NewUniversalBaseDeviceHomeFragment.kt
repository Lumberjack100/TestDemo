package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.baseclickproxy.DoubleClickListener
import com.shmedo.mcloudapp.databinding.FragmentUniversalDeviceHomeNewBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.LocationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.AdvancedSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonDeviceHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2025/6/9
 * 描述： 通用设备配置主页面抽象基类 - 支持4G和蓝牙两种通讯方式
 */
abstract class NewUniversalBaseDeviceHomeFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentUniversalDeviceHomeNewBinding
    protected val toolbarViewModel: ToolbarViewModel by viewModels()
    protected val mHeadStates: CommonDeviceHomeViewModel by viewModels()
    protected val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    protected val iotParseManager: IOTParserManager by inject()

    // 位置同步相关
    private val locationViewModel: LocationViewModel by activityViewModel()
    private val locationSyncViewModel: AdvancedSettingViewModel by viewModels()
    private var gcjLatLng: BDLocation? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）
    private var isLocationSyncInProgress = false // 添加标志位，防止重复同步

    private var lastOnlineStatus: Boolean = false//在线状态
    private var deviceStatusCheckJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_universal_device_home_new,
            BR.stateVM,
            mHeadStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDeviceHomeNewBinding
        binding.llToolbar.toolbar.title = "返回"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        registerOnBackPressedDispatcher {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        initDeviceLogoDoubleClickListener()
        initModuleAdapter()
    }

    private fun initDeviceLogoDoubleClickListener() {
        binding.llDeviceInfo.ivDeviceLogo.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View) {
                if (isBleDisconnected() || isNetDisconnected()) {
                    return
                }
                searchDevice()
            }
        })
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(communicateWay is BleConnect)

        mHeadStates.productName.set(productType.productName.ifEmpty { deviceInfo.productName })
        val deviceName =
            if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken }
        mHeadStates.productToken.set(productType.productToken.ifEmpty { deviceName })
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)

        initModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        if (isConnected) {
            mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
            mHeadStates.iotPlatformStateText.set("蓝牙已连接")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_disconnect)

        } else {
            mHeadStates.productLogoResId.set(mHeadStates.productOfflineResId.get())
            mHeadStates.iotPlatformStateText.set("蓝牙已断开")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_connect)

            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
        }

        //刷新模块状态
        binding.rvModule.models?.forEach { item ->
            if (item is ConfigModuleTree) {
                item.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(isConnected)
                }
            }
        }
    }

    private fun initModuleAdapter() {
        binding.rvModule.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group2)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    4,
                                    ConvertUtils.dp2px(10f), false
                                )
                            )
                            addType<ConfigModule>(R.layout.item_device_config_module_ud)
                            R.id.item.onClick {
                                val configModule = getModel<ConfigModule>()
                                processSubModuleItemClick(configModule.functionModule)
                            }
                        }
                    }

                    else -> {}
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val configModuleTree = getModel<ConfigModuleTree>()
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.models = configModuleTree.configModules
                    }

                    else -> {
                        processOtherItemViewBind(itemViewType)
                    }
                }
            }
        }
    }

    protected open fun BindingViewHolder.processOtherItemViewBind(viewId: Int) {}

    protected abstract fun initModuleData()

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }
    }

    private fun processSubModuleItemClick(module: DeviceFunctionModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module) {
            else -> {
                processOtherItemClick(module)
            }
        }
    }

    protected open fun processOtherItemClick(configModule: DeviceFunctionModule) {
        if (configModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                configModule.navId,
                bundle
            )
        } else {
            Toaster.show("正在开发中")
        }
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            onNetPlatformReady()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    private fun onNetPlatformReady() {
        lastOnlineStatus = deviceInfo.onlineStatus
        if (deviceInfo.onlineStatus) {
            mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
            mHeadStates.iotPlatformStateText.set("米度平台在线")
            queryStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(mHeadStates.productOfflineResId.get())
            mHeadStates.iotPlatformStateText.set("米度平台离线")

            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
        }
        //刷新模块状态
        binding.rvModule.models?.forEach { item ->
            if (item is ConfigModuleTree) {
                item.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(deviceInfo.onlineStatus)
                }
            }
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        queryStatusInfo()
        // 自动同步位置（如果需要）
        autoSyncLocationIfNeeded()
    }

    open fun queryStatusInfo() {

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
            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "设备查找出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = false,
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
            IOTCommandType.MD_SEARCH_DEVICE -> {
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
                    isShowErrMsg = false,
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
            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
                    errMsg = errMsg
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.TAG) {
                                FindDeviceBeepDialog.newInstance(productType)
                            }
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_INSTALL_LOCATION -> {
                handleLocationSyncResult(cmdStr)
            }

            else -> {
                processOtherCmdResult(
                    IOTCommandUtil.extractCommandType(cmdStr),
                    cmdStr
                )
            }
        }
    }

    protected open fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {}

    override fun createObserver() {
        super.createObserver()
        if (communicateWay is NetPlatformConnect) {
            checkDeviceOnlineStatus()
        } else {
            setupHeartbeat()
        }

        // 观察定位信息
        if (isNeedAutoSyncLocation()) {
            // 观察位置服务错误状态
            launchAndRepeatWithViewLifecycle(minActiveState = Lifecycle.State.STARTED) {
                locationViewModel.locationErrorState.collectLatest { error ->
                    Timber.w("位置服务错误: ${error.message}")
                    // 如果是自动同步过程中的错误，停止同步
                    if (isLocationSyncInProgress) {
                        isLocationSyncInProgress = false
                    }
                }
            }
        }
    }

    /**
     * 设置心跳检查
     */
    protected open fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_BLE_HEART_BEAT)  //20秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime =
                        TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    //仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get()) {
                        Timber.Forest.d("发送心跳包指令 startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.Forest.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }

    private fun checkDeviceOnlineStatus() {
        // 取消现有的job
        deviceStatusCheckJob?.cancel()

        // 创建新的job，每30秒执行一次
        deviceStatusCheckJob = launchWithViewLifecycle {
            while (isActive) {
                try {
                    deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                        addDeviceLogItem(Log.ERROR, error.errorMsg)
                    }?.let { deviceDetailInfo ->
                        // 如果设备在线状态发生变化，更新UI
                        if (deviceDetailInfo.deviceInfo.onlineStatus != lastOnlineStatus) {
                            onNetPlatformReady()
                        }
                    }
                } catch (e: Exception) {
                    Timber.Forest.e(e)
                }
                delay(AppContants.Communication.DELAY_CHECK_DEVICE_ONLINE_STATUS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    // 在 onDestroy 中取消 job
    override fun onDestroy() {
        super.onDestroy()
        deviceStatusCheckJob?.cancel()
        deviceStatusCheckJob = null
        // 不要在这里停止位置服务，因为 LocationViewModel 是 Activity 级别的
        // locationViewModel.stopLocation()
    }

    //<editor-fold desc="自动位置同步相关">
    /**
     * 判断是否需要自动同步位置
     */
    private fun isNeedAutoSyncLocation(): Boolean {
        return communicateWay is BleConnect &&
                (productType == ProductType.U_I_1
                        || productType == ProductType.U_R_1
                        || productType == ProductType.BHY
                        || productType == ProductType.LB20S
                        || productType == ProductType.LR200

                        || productType == ProductType.COLLECTOR_R_1
                        || productType == ProductType.COLLECTOR_R_2
                        || productType == ProductType.DAS
                        )
    }

    /**
     * 自动同步位置（如果需要）
     */
    protected fun autoSyncLocationIfNeeded() {
        if (!isNeedAutoSyncLocation()) {
            return
        }
        
        // 如果已经在同步中，则不重复执行
        if (isLocationSyncInProgress) {
            Timber.d("位置同步已在进行中，跳过重复同步")
            return
        }

        // 延迟1秒后开始位置同步，确保设备连接稳定
        launchWithViewLifecycle {
            delay(1000)
            checkPermissionsForAutoSync()
        }
    }

    /**
     * 检查权限并进行自动同步
     */
    private fun checkPermissionsForAutoSync() {
        if (PermissionHelper.isLocationEnabled()) {
            checkPermissionForLocation()
        } else {
            // 如果位置服务未开启，显示提示但不强制开启
            Toaster.show("位置服务未开启，无法自动同步设备位置")
        }
    }

    private fun checkPermissionForLocation() {
        XXPermissions.with(this)
            // 申请多个权限
            .permission(PermissionHelper.foregroundLocationPermissions)
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    grantedPermissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    if (allGranted) {
                        // 权限获取成功，开始自动同步位置
                        startAutoLocationSync()
                    }
                }

                override fun onDenied(
                    deniedPermissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    // 权限被拒绝，不进行强制提示，静默处理
                    Timber.d("位置权限被拒绝，跳过自动位置同步")
                }
            })
    }

    /**
     * 开始自动位置同步
     */
    private fun startAutoLocationSync() {
        isLocationSyncInProgress = true
        gcjLatLng = null
        
        // 首先尝试获取缓存的位置
        val cachedLocation = locationViewModel.getCachedLocation()
        if (cachedLocation != null) {
            Timber.d("使用缓存位置进行自动同步")
            processLocationForSync(cachedLocation)
            return
        }
        
        // 没有缓存位置，请求新的位置
        locationViewModel.requestImmediateLocationUpdate { location ->
            if (location != null) {
                Timber.d("获取到新位置，开始自动同步")
                processLocationForSync(location)
            } else {
                Timber.d("自动位置同步：未能获取到位置信息")
                isLocationSyncInProgress = false
            }
        }
    }
    
    /**
     * 处理位置信息用于同步
     */
    private fun processLocationForSync(bdLocation: BDLocation) {
        gcjLatLng = bdLocation
        //将GCJ-02火星坐标转换为WGS-84世界标准地理坐标
        val mWgsLatLng = JZLocationConverter.gcj02ToWgs84(
            CustomLatLng(
                bdLocation.latitude,
                bdLocation.longitude
            )
        )
        locationSyncViewModel.location.set(
            Html.fromHtml(
                String.format(
                    Locale.getDefault(),
                    "%.8f,%.8f",
                    mWgsLatLng.longitude,
                    mWgsLatLng.latitude
                )
            ).toString()
        )
        locationSyncViewModel.latitude.set(mWgsLatLng.latitude.toString())
        locationSyncViewModel.longitude.set(mWgsLatLng.longitude.toString())
        
        // 执行位置同步
        performLocationSync()
    }

    /**
     * 执行位置同步
     */
    private fun performLocationSync() {
        if (isBleDisconnected()) {
            return
        }

        commandItems.clear()
        val command = when (productType) {
            ProductType.U_I_1,
            ProductType.U_R_1,
            ProductType.BHY,
            ProductType.DAS,
            ProductType.COLLECTOR_R_1 -> {
                //##9161
                MDCommandUtil.getCommand(
                    MDCommandType.INSTALL_LOCATION,
                    "1${locationSyncViewModel.location.get()}"
                )
            }

            else -> {
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_SET_INSTALL_LOCATION,
                    "lat=${locationSyncViewModel.latitude.get()}&lng=${locationSyncViewModel.longitude.get()}"
                )
            }
        }

        commandItems.add(command)

        Timber.d("自动同步位置指令: $command")
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }
    //</editor-fold>

    private fun handleLocationSyncResult(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "位置同步失败: ${result.message}"
                Timber.e(errMsg)
                // 自动同步失败时不显示错误提示，静默处理
                cancelNearbyCommunicationTimeoutJob()
                isLocationSyncInProgress = false
            }

            else -> {
                sendCommandFromCmdList {
                    Timber.d("位置自动同步成功")
                    isLocationSyncInProgress = false
                    // 可选：显示成功提示
                    // Toaster.show("位置已自动同步")
                }
            }
        }
    }
    //</editor-fold>
}