package com.shmedo.mcloudapp.ui.page.userprofile

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAppPermissionSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AppPermissionSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionHelper

class AppPermissionSettingFragment : BaseFragment() {
    private lateinit var binding: FragmentAppPermissionSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AppPermissionSettingViewModel by viewModels()

    private val needBluetoothPermissions by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        else
            arrayOf()
    }

    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_app_permission_setting, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAppPermissionSettingBinding
        binding.llToolbar.toolbar.title = "系统权限设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
    }

    override fun createObserver() {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
        checkLocationPermission()
        checkBluetoothPermission()
        checkCameraPermission()
        checkStoragePermission()
    }

    private fun checkLocationPermission() {
        mStates.isAllowLocation.set(
            XXPermissions.isGranted(
                requireContext(),
                PermissionHelper.foregroundLocationPermissions
            )
        )
    }

    private fun checkBluetoothPermission() {
        mStates.isAllowBluetooth.set(
            XXPermissions.isGranted(
                requireContext(),
                needBluetoothPermissions
            )
        )
    }

    private fun checkCameraPermission() {
        mStates.isAllowCamera.set(
            XXPermissions.isGranted(
                requireContext(),
                Permission.CAMERA
            )
        )
    }

    private fun checkStoragePermission() {
        mStates.isAllowStorage.set(
            XXPermissions.isGranted(
                requireContext(),
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onLocationPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                PermissionHelper.foregroundLocationPermissions
            )
        }

        fun onBluetoothPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                needBluetoothPermissions
            )
        }

        fun onCameraPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                Permission.CAMERA
            )
        }

        fun onStoragePermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        }
    }
}