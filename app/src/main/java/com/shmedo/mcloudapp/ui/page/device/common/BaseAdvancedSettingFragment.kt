package com.shmedo.mcloudapp.ui.page.device.common

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import com.baidu.location.BDLocation
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
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDLowEnergyModel
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdvancedSettingBinding
import com.shmedo.mcloudapp.databinding.ItemAdvancedSettingBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
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
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter
import com.shmedo.mcloudapp.utils.permission.PermissionHelper
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/17
 * 描述： 系统配置页面
 */
open class BaseAdvancedSettingFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdvancedSettingViewModel
    private val locationViewModel: LocationViewModel by activityViewModel()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private var gcjLatLng: BDLocation? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）

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

        if (communicateWay is NetPlatformConnect) {
            if (productType != ProductType.COLLECTOR_G_0) {
                moduleList.add(
                    AdvancedSettingItem(
                        "固件升级",
                        AdvancedSettingItem.Type.FIRMWARE,
                    )
                )
            }
        }

        moduleList.add(
            AdvancedSettingItem(
                "重启",
                AdvancedSettingItem.Type.REBOOT,
            )
        )

        if (productType != ProductType.COLLECTOR_G_0) {
            moduleList.add(
                AdvancedSettingItem(
                    "恢复出厂设置",
                    AdvancedSettingItem.Type.RESET,
                )
            )
        }

        if (communicateWay is BleConnect) {
            if (productType != ProductType.COLLECTOR_G_0) {
                moduleList.add(
                    AdvancedSettingItem(
                        "远程调试",
                        AdvancedSettingItem.Type.REMOTE_DEBUG,
                    )
                )
            }

            if (productType == ProductType.U_I_1) {
                moduleList.add(
                    AdvancedSettingItem(
                        "一键仓储",
                        AdvancedSettingItem.Type.STANDBY,
                    )
                )
            }
        }

        binding.recyclerview.models = moduleList
    }

    private fun isNeedSyncLocation(): Boolean {
        return communicateWay is BleConnect &&
                (productType == ProductType.LR200
                        || productType == ProductType.LB20S
                        || productType == ProductType.U_R_1
                        || productType == ProductType.U_I_1)
    }

    /**
     * 是否需要初始化偏移量
     */
    private fun isNeedOffsetInitialization(): Boolean {
        return productType == ProductType.M20
                || productType == ProductType.GNSS_M_1
                || productType == ProductType.GNSS_M_2
                || productType == ProductType.COLLECTOR_R_3
    }

    override fun createObserver() {
        super.createObserver()
        //观察定位信息
        launchAndRepeatWithViewLifecycle(minActiveState = Lifecycle.State.STARTED) {
            locationViewModel.locationState.collectLatest { bdLocation ->
                if (gcjLatLng == null || gcjLatLng!!.latitude == 0.0 || gcjLatLng!!.longitude == 0.0) {
                    gcjLatLng = bdLocation
                    //将GCJ-02火星坐标转换为WGS-84世界标准地理坐标
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

    protected open fun processItemClick(item: AdvancedSettingItem) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (item.type) {
            AdvancedSettingItem.Type.REBOOT -> {
                showMessage("确定重启吗？", "温馨提示", "确定", {
                    reboot()
                }, "取消")
            }

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
                nav().safeNavigate(R.id.action_global_to_firmwareUpgradeFragment, bundle)
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
                nav().safeNavigate(R.id.action_global_to_remoteDebugFragment, bundle)
            }

            AdvancedSettingItem.Type.STANDBY -> {
                showMessage("确定进入仓储模式吗？", "温馨提示", "确定", {
                    setStandByMode()
                }, "取消")
            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
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

            IOTCommandType.REBOOT -> {//重启设备
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

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

            else -> {
                when (MDCommandUtil.extractCommandType(cmdStr)) {
                    MDCommandType.LOW_ENERGY -> {//
                        when (val result = mdParseManager.parse<String>(cmdStr)) {
                            is MDCommandResult.Failure -> {
                                val errMsg = "设置仓储模式出错!"
                                handleFailureResult(errMsg)
                                return
                            }

                            else -> {
                                sendCommandFromCmdList {
                                    Toaster.show("设置仓储模式成功")
                                }
                            }
                        }
                    }

                    else -> {
                        cancelNearbyCommunicationTimeoutJob()
                    }
                }
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
                    mStates.refreshingLocation.set(true)
                    gcjLatLng = null
                    locationViewModel.requestImmediateLocationUpdate()
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
        mStates.refreshingLocation.set(true)
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

    /**
     * 打开/关闭设备低功耗模式
     */
    private fun setStandByMode() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.LOW_ENERGY, MDLowEnergyModel.STANDBY.toString()
        )
        commandItems.add(command)
        Timber.d("打开设备低功耗模式指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
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
    }
}