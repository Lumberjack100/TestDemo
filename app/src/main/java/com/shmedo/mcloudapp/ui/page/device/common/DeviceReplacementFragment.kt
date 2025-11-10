package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceBackupInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.enums.FailureChildType
import com.shmedo.lib.cmd.base.iot_cmd.enums.FailureMainType
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.PageList
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDeviceReplacementBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceReplacementViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author：gonghe
 * @time: 2025/8/11
 * @desc: 设备更换
 *
 */
class DeviceReplacementFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceReplacementBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DeviceReplacementViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private lateinit var oldDeviceInfo: DeviceInfo

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_device_replacement,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceReplacementBinding
        binding.llToolbar.toolbar.title = "设备更换"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
        // 从参数中获取设备信息
        arguments?.let {
            oldDeviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
        }
        mStates.oldDeviceToken.set(oldDeviceInfo.deviceToken)
        mStates.oldFirmwareVersion.set(oldDeviceInfo.firmwareVersion)
    }

    override fun createObserver() {
        setFragmentResultListener(AppContants.Extras.FRAGMENT_COMMON_RESULT_REQUEST_KEY) { _, bundle ->
            handleFragmentResult(bundle)
        }
    }

    private fun handleFragmentResult(bundle: Bundle) {
        val newDeviceInfo: DeviceInfo? = bundle.getParcelable(AppContants.Extras.DEVICE_INFO)
        newDeviceInfo?.let { info ->
            mStates.newDeviceToken.set(info.deviceToken)
            mStates.newFirmwareVersion.set(info.firmwareVersion)
            getLatestBackup(info.id.toString())
        }
    }

    private fun getLatestBackup(deviceID: String) {
        launchWithViewLifecycle {
            val data: PageList<DeviceBackupInfo> = deviceRequestViewModel.queryDeviceBackupList(
                deviceID = deviceID,
                pageSize = 1
            ) { error: Throwable ->
                error.printStackTrace()
            } ?: return@launchWithViewLifecycle

            if (data.currentPageData.isNullOrEmpty()) {
                return@launchWithViewLifecycle
            }
            val backupInfo = data.currentPageData!![0]
            mStates.backupInfo.set("备份版本：${backupInfo.backupVersion}   备份类型：${backupInfo.backupTypeStr}   备份时间：${backupInfo.backupTime}")
        }
    }

    override fun lazyLoadData() {

    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 跳转到选择设备页面
         */
        fun onGoToSelectDevice() {
            nav().safeNavigate(R.id.action_global_to_deviceSelectFragment)
        }

        /**
         * 切换故障主类型
         */
        fun onToggleFailureMainType() {
            val failureMainTypeList = FailureMainType.typeNames
            val selectedIndex = failureMainTypeList.indexOf(mStates.failureMainType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", failureMainTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.failureMainType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 切换故障子类型
         */
        fun onToggleFailureChildType() {
            val failureMainType = FailureMainType.valueByTypeName(mStates.failureMainType.get())
            val failureChildTypeList = FailureChildType.getChildTypeNamesByMainType(failureMainType)
            val selectedIndex = failureChildTypeList.indexOf(mStates.failureChildType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", failureChildTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.failureChildType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            if (mStates.newDeviceToken.get().isEmpty()) {
                Toaster.show("请设置新设备SN号")
                return
            }
            if (mStates.newDeviceToken.get() == mStates.oldDeviceToken.get()) {
                Toaster.show("新设备SN号不能与旧设备SN号相同")
                return
            }
            if (mStates.failureMainType.get().isEmpty()) {
                Toaster.show("请选择故障主类型")
                return
            }
            if (mStates.failureChildType.get().isEmpty()) {
                Toaster.show("请选择故障子类型")
                return
            }
            if (mStates.failureDesc.get().isEmpty()) {
                Toaster.show("请输入故障描述")
                return
            }
            showLoadingDialog(StringUtils.getString(R.string.processing))
            replaceDevice()
        }
    }

    private fun replaceDevice() {
        launchWithViewLifecycle {
            deviceRequestViewModel.replaceDevice(
                companyID = AuthMMKVOwner.companyID,
                oldDeviceToken = mStates.oldDeviceToken.get(),
                newDeviceToken = mStates.newDeviceToken.get(),
                configEnable = mStates.configEnable.get(),
                failureMainType = FailureMainType.valueByTypeName(mStates.failureMainType.get())
                    .getCode(),
                failureChildType = FailureChildType.valueByTypeName(mStates.failureChildType.get())
                    .getCode(),
                failureDesc = mStates.failureDesc.get(),
                replaceTime = TimeUtils.getNowString(),
                replacePerson = AuthMMKVOwner.realName

            ) { error: Throwable ->
                dismissLoadingDialog()
                showMessageDialog("更换设备失败，${error.errorMsg}")
            } ?: return@launchWithViewLifecycle

            dismissLoadingDialog()
            Toaster.show("更换设备成功")
            delay(1000)
        }
    }


    companion object {
        /**
         * 创建Bundle参数 (兼容原有接口)
         */
        fun newBundleArguments(
            deviceInfo: DeviceInfo,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}