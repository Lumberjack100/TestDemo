package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.StringUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseDasAdvancedSettingClickProxy
import com.shmedo.mcloudapp.databinding.FragmentBleDasAdvancedSettingBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.SyncInstallationLocationPopupView
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
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
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
@Deprecated("This class is deprecated", ReplaceWith("BaseAdvancedSettingFragment"))
class BleDasAdvancedSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdvancedSettingViewModel
    private val locationViewModel: LocationViewModel by activityViewModel()
    private val mdParseManager: MDParserManager by inject()

    private var gcjLatLng: BDLocation? = null //当前定位经纬度,中国国测局地理坐标（GCJ-02）


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ble_das_advanced_setting,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleDasAdvancedSettingBinding
        binding.llToolbar.toolbar.title = "设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }


    override fun initData() {
        super.initData()
        checkPermissions()
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
                            String.Companion.format(
                                Locale.getDefault(),
                                "%.8f,%.8f",
                                mWgsLatLng.longitude,
                                mWgsLatLng.latitude
                            )
                        ).toString()
                    )

                    mStates.address.set(bdLocation.addrStr ?: "")
                    mStates.refreshingLocation.set(false)
                }
            }
        }
    }

    inner class ClickProxy : BaseDasAdvancedSettingClickProxy() {
        override fun onSyncLocationClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showSyncInstallationLocationPopup()
        }

        override fun onCommandDebugClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = BleCustomCommandLogPrintFragment.Companion.newBundleArguments(
                false,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(R.id.action_global_to_commandDebug, bundle)
        }

        override fun onResetClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command = MDCommandUtil.getCommand(MDCommandType.RESTORE_FACTORY_SETTING)
                commandItems.add(command)
                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        override fun onRemoteDebuggingClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(R.id.action_global_to_remoteDebugFragment, bundle)
        }
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

                    commandItems.clear()
                    //##9161
                    val command = MDCommandUtil.getCommand(
                        MDCommandType.INSTALL_LOCATION,
                        "1${mStates.location.get()}"
                    )
                    commandItems.add(command)
                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.INSTALL_LOCATION -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
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

            MDCommandType.RESTORE_FACTORY_SETTING -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reset_failed)
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
                cancelNearbyCommunicationTimeoutJob()
            }
        }
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