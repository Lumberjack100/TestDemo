package com.shmedo.mcloudapp.ui.page.device.common

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDLowEnergyModel
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentAdvancedSettingBinding
import com.shmedo.mcloudapp.databinding.ItemAdvancedSettingBinding
import com.shmedo.mcloudapp.extensions.isGTSeries
import com.shmedo.mcloudapp.extensions.isGateway
import com.shmedo.mcloudapp.extensions.isM20Series
import com.shmedo.mcloudapp.extensions.isM50Series
import com.shmedo.mcloudapp.extensions.isSupportFirmwareUpgrade
import com.shmedo.mcloudapp.extensions.isUIProduct
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.AdvancedSettingItem
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.SyncInstallationLocationPopupView
import com.shmedo.mcloudapp.ui.viewmodel.request.LocationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.AdvancedSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2025/1/23
 * @desc: 系统配置页面
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseIOTDeviceFragment，使用新的通信架构
 * 2. 使用 sendCommandSequence 统一指令发送
 * 3. 在 handleCommandResponse 中统一处理所有响应
 * 4. 简化错误处理逻辑，利用基类的统一错误处理
 * 5. 保持原有业务逻辑和功能不变
 */
class AdvancedSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdvancedSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdvancedSettingViewModel by viewModels()
    private val locationViewModel: LocationViewModel by activityViewModel()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private var gcjLatLng: BDLocation? = null // 当前定位经纬度，中国国测局地理坐标（GCJ-02）

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
        binding.llToolbar.toolbar.title = "系统配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initAdapter()
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            addType<AdvancedSettingItem>(R.layout.item_advanced_setting)
            onBind {
                val itemBinding = getBinding<ItemAdvancedSettingBinding>()
                when (modelPosition) {
                    0 -> itemBinding.item.setBackgroundResource(R.drawable.layer_common_click_item_top_corner_4_with_divider)
                    modelCount - 1 -> itemBinding.item.setBackgroundResource(R.drawable.shape_common_click_item_bottom_corner_4)
                    else -> itemBinding.item.setBackgroundResource(R.drawable.layer_common_click_item_with_divider)
                }
            }
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
        val moduleList: MutableList<AdvancedSettingItem> = mutableListOf()

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

        if (communicateWay is NetPlatformConnect && isSupportFirmwareUpgrade()) {
            moduleList.add(
                AdvancedSettingItem(
                    "固件升级",
                    AdvancedSettingItem.Type.FIRMWARE_UPGRADE,
                )
            )
        }

        moduleList.add(
            AdvancedSettingItem(
                "重启",
                AdvancedSettingItem.Type.REBOOT,
            )
        )

        if (communicateWay is BleConnect && isSupportHibernation()) {
            moduleList.add(
                AdvancedSettingItem(
                    "设备休眠",
                    AdvancedSettingItem.Type.HIBERNATION,
                )
            )
        }

        if (!productType.isGateway()) {
            moduleList.add(
                AdvancedSettingItem(
                    "恢复出厂设置",
                    AdvancedSettingItem.Type.RESET,
                )
            )
        }

        if (isSupportFormatDataStorage()) {
            moduleList.add(
                AdvancedSettingItem(
                    "格式化数据存储",
                    AdvancedSettingItem.Type.FORMAT_DATA_STORAGE,
                )
            )
        }

        if (communicateWay is BleConnect) {
            if (productType.isUIProduct()) {
                moduleList.add(
                    AdvancedSettingItem(
                        "一键仓储",
                        AdvancedSettingItem.Type.STANDBY,
                    )
                )
            }

            // 添加监测数据导出功能
            if (isSupportMonitoringDataExport()) {
                moduleList.add(
                    AdvancedSettingItem(
                        "监测数据导出",
                        AdvancedSettingItem.Type.MONITORING_DATA_EXPORT,
                    )
                )
            }

            if (!productType.isGateway()) {
                moduleList.add(
                    AdvancedSettingItem(
                        "远程调试",
                        AdvancedSettingItem.Type.REMOTE_DEBUG,
                    )
                )
            }
        }

        if (isSupportReplace()) {
            moduleList.add(
                AdvancedSettingItem(
                    "更换设备",
                    AdvancedSettingItem.Type.REPLACE_DEVICE,
                )
            )
        }

        binding.recyclerview.models = moduleList
    }

    /**
     * 是否需要同步位置
     */
    private fun isNeedSyncLocation(): Boolean {
        // TODO: 根据需要启用相应的设备类型
        // return communicateWay is BleConnect &&
        //         (productType == ProductType.LR200
        //                 || productType == ProductType.LB20S
        //                 || productType == ProductType.U_R_1
        //                 || productType == ProductType.U_I_1)
        return false
    }

    /**
     * 是否需要初始化偏移量
     */
    private fun isNeedOffsetInitialization(): Boolean {
        return productType.isM20Series()
    }

    /**
     * 是否支持固件升级
     */
    private fun isSupportFirmwareUpgrade(): Boolean {
        return productType.isSupportFirmwareUpgrade()
    }

    /**
     * 是否支持休眠
     */
    private fun isSupportHibernation(): Boolean {
        return productType.isM50Series()
    }


    /**
     * 是否支持格式化数据存储
     */
    private fun isSupportFormatDataStorage(): Boolean {
        return productType.isM20Series() || productType.isM50Series()
    }

    /**
     * 是否支持监测数据导出
     */
    private fun isSupportMonitoringDataExport(): Boolean {
        // TODO: 根据需要启用相应的设备类型
        // return productType == ProductType.COLLECTOR_R_1
        //         || productType == ProductType.DAS
        return false
    }

    /**
     * 是否支持更换新设备
     */
    private fun isSupportReplace(): Boolean {
        return productType != ProductType.U_L_1
                && productType != ProductType.COLLECTOR_G_0
                && !productType.isGTSeries()
    }

    override fun createObserver() {
        super.createObserver()
        if (isNeedSyncLocation()) {
            // 观察定位信息
            launchAndRepeatWithViewLifecycle(minActiveState = Lifecycle.State.STARTED) {
                locationViewModel.locationState.collectLatest { bdLocation ->
                    if (gcjLatLng == null || gcjLatLng!!.latitude == 0.0 || gcjLatLng!!.longitude == 0.0) {
                        gcjLatLng = bdLocation
                        // 将GCJ-02火星坐标转换为WGS-84世界标准地理坐标
                        val mWgsLatLng = JZLocationConverter.gcj02ToWgs84(
                            CustomLatLng(
                                bdLocation.latitude,
                                bdLocation.longitude
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

                        mStates.address.set(bdLocation.addrStr ?: "")
                        mStates.refreshingLocation.set(false)
                    }
                }
            }
        }
    }

    private fun processItemClick(item: AdvancedSettingItem) {
        if (!isDeviceConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }

        when (item.type) {
            AdvancedSettingItem.Type.SYNC_INSTALL_POSITION -> {//同步安装位置
                showSyncInstallationLocationPopup()
            }

            AdvancedSettingItem.Type.OFFSET_INITIALIZATION -> {//偏移初始化
                showMessage("确定进行告警偏移初始化吗？", "温馨提示", "确定", {
                    executeOffsetInitialization()
                }, "取消")
            }

            AdvancedSettingItem.Type.FIRMWARE_UPGRADE -> {//固件升级
                val bundle = newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(R.id.action_global_to_firmwareUpgradeFragment, bundle)
            }

            AdvancedSettingItem.Type.REBOOT -> {//重启
                showMessage("确定重启吗？", "温馨提示", "确定", {
                    executeReboot()
                }, "取消")
            }

            AdvancedSettingItem.Type.HIBERNATION -> {//休眠
                showMessage("确定休眠吗？", "温馨提示", "确定", {
                    executeHibernate()
                }, "取消")
            }

            AdvancedSettingItem.Type.RESET -> {//恢复出厂设置
                showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                    executeReset()
                }, "取消")
            }

            AdvancedSettingItem.Type.FORMAT_DATA_STORAGE -> {//格式化数据存储
                showMessage("确定格式化数据吗？", "温馨提示", "确定", {
                    executeFormatDataStorage()
                }, "取消")
            }

            AdvancedSettingItem.Type.STANDBY -> {//待机进入仓储模式
                showMessage("确定进入仓储模式吗？", "温馨提示", "确定", {
                    executeStandByMode()
                }, "取消")
            }

            AdvancedSettingItem.Type.MONITORING_DATA_EXPORT -> {//监测数据导出
                // 跳转到监测数据导出页面
                val bundle = newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                ).apply {
                    putString("deviceSn", deviceInfo.deviceToken)
                    putString("apiKey", deviceInfo.apikey)
                }
                nav().safeNavigate(R.id.action_global_to_monitoringDataExportFragment, bundle)
            }

            AdvancedSettingItem.Type.REMOTE_DEBUG -> {//远程调试
                val bundle = newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(R.id.action_global_to_remoteDebugFragment, bundle)
            }

            AdvancedSettingItem.Type.REPLACE_DEVICE -> { //更换设备
                val bundle = DeviceReplacementFragment.newBundleArguments(
                    deviceInfo
                )
                nav().safeNavigate(R.id.action_global_to_deviceReplacementFragment, bundle)
            }


            else -> {}
        }
    }

    // ==================== 指令执行方法 ====================
    /**
     * 执行设置安装位置
     */
    private fun executeSetInstallationLocation() {
        val command = if (productType == ProductType.U_R_1 || productType == ProductType.U_I_1) {
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_RAW, "content=##9161${mStates.location.get()}"
            )
        } else {
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_INSTALL_LOCATION,
                "lat=${mStates.latitude.get()}&lng=${mStates.longitude.get()}"
            )
        }

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 执行告警偏移初始化
     */
    private fun executeOffsetInitialization() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            "method=1&type=gnss"
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 执行重启指令
     */
    private fun executeReboot() {
        if (isBleDas()) {
            val command = MDCommandUtil.getCommand(MDCommandType.REBOOT_DEVICE, "1")
            sendCommandSequence(
                commands = listOf(command),
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        } else {
            rebootDevice(
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        }
    }

    /**
     * 执行休眠指令
     */
    private fun executeHibernate() {
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.SET_WORK_MODE, "sw=0&factory_sw=1&mode=3")
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 执行恢复出厂设置指令
     */
    private fun executeReset() {
        if (isBleDas()) {
            val command = MDCommandUtil.getCommand(MDCommandType.RESTORE_FACTORY_SETTING)
            sendCommandSequence(
                commands = listOf(command),
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        } else {
            restoreFactory(
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        }
    }

    /**
     * 执行格式化数据存储
     */
    private fun executeFormatDataStorage() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_FORMAT_DATA_STORAGE, "type=1")
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 执行一键仓储模式
     */
    private fun executeStandByMode() {
        val command = MDCommandUtil.getCommand(
            MDCommandType.LOW_ENERGY, MDLowEnergyModel.STANDBY.toString()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }


    // ==================== 响应处理 ====================

    /**
     * 处理指令响应 - 统一处理所有指令的响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        // 处理 IOT 指令响应
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SET_INSTALL_LOCATION -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "同步安装位置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("同步安装位置成功")
                    }
                }
            }

            IOTCommandType.MD_RAW -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "同步安装位置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("同步安装位置成功")
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "初始化失败: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("初始化完成")
                    }
                }
            }

            IOTCommandType.REBOOT -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                    }
                }
            }

            IOTCommandType.SET_WORK_MODE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            StringUtils.getString(R.string.hibernate_failed) + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show(StringUtils.getString(R.string.device_hibernation_tip))
                    }
                }
            }

            IOTCommandType.RESET -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reset_failed) + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                    }
                }
            }

            IOTCommandType.MD_FORMAT_DATA_STORAGE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "格式化数据出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("格式化数据成功")
                    }
                }
            }

            else -> {
                // 处理 MD 指令响应
                when (MDCommandUtil.extractCommandType(cmdStr)) {
                    MDCommandType.REBOOT_DEVICE -> {
                        val result = mdParseManager.parse<String>(cmdStr)
                        when (result) {
                            is MDCommandResult.Failure -> {
                                val errMsg = StringUtils.getString(R.string.reboot_failed)
                                handleFailureResult(errMsg, isMessageDialog = true)
                            }

                            else -> {
                                Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                            }
                        }
                    }

                    MDCommandType.RESTORE_FACTORY_SETTING -> {
                        val result = mdParseManager.parse<String>(cmdStr)
                        when (result) {
                            is MDCommandResult.Failure -> {
                                val errMsg = StringUtils.getString(R.string.reset_failed)
                                handleFailureResult(errMsg, isMessageDialog = true)
                            }

                            else -> {
                                Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                            }
                        }
                    }

                    MDCommandType.LOW_ENERGY -> {
                        val result = mdParseManager.parse<String>(cmdStr)
                        when (result) {
                            is MDCommandResult.Failure -> {
                                val errMsg = "设置仓储模式出错!"
                                handleFailureResult(errMsg, isMessageDialog = true)
                            }

                            else -> {
                                Toaster.show("设置仓储模式成功")
                            }
                        }
                    }

                    else -> {
                        Timber.d("未处理的指令类型: $cmdStr")
                    }
                }
            }
        }
    }

    // ==================== 定位和弹窗相关 ====================

    /**
     * 显示同步安装位置弹窗
     */
    private fun showSyncInstallationLocationPopup() {
        val popupView = SyncInstallationLocationPopupView(requireContext())
        popupView.setTitle("同步安装位置", mStates)
            .setClickListener(object : SyncInstallationLocationPopupView.OnClickListener {
                override fun onRefreshingLocationClick() {
                    mStates.refreshingLocation.set(true)
                    gcjLatLng = null
                    locationViewModel.requestImmediateLocationUpdate()
                }

                override fun onConfirmClick() {
                    locationViewModel.stopLocation()
                    executeSetInstallationLocation()
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .enableDrag(false)
            .isDestroyOnDismiss(true)
            .asCustom(popupView)
            .show()

        gcjLatLng = null
        locationViewModel.requestImmediateLocationUpdate()
        mStates.refreshingLocation.set(true)
    }

    // ==================== 权限相关 ====================

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
        checkPermissionForLocation()
    }

    private fun checkPermissionForLocation() {
        XXPermissions.with(this)
            .permission(PermissionLists.getAccessFineLocationPermission())
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: List<IPermission>, deniedList: List<IPermission>
                ) {
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        return
                    }
                    locationViewModel.requestImmediateLocationUpdate()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocation()
    }
}
