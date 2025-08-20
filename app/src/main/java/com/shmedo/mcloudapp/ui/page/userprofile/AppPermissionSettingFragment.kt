package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAppPermissionSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AppPermissionSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

class AppPermissionSettingFragment : BaseFragment() {
    private lateinit var binding: FragmentAppPermissionSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AppPermissionSettingViewModel by viewModels()

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
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
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
            XXPermissions.isGrantedPermissions(
                requireContext(),
                listOf(
                    PermissionLists.getAccessCoarseLocationPermission(),
                    PermissionLists.getAccessFineLocationPermission()
                )
            )
        )
    }

    private fun checkBluetoothPermission() {
        mStates.isAllowBluetooth.set(
            XXPermissions.isGrantedPermissions(
                requireContext(),
                listOf(
                    PermissionLists.getBluetoothScanPermission(),
                    PermissionLists.getBluetoothConnectPermission()
                )
            )
        )
    }

    private fun checkCameraPermission() {
        mStates.isAllowCamera.set(
            XXPermissions.isGrantedPermission(
                requireContext(),
                PermissionLists.getCameraPermission()
            )
        )
    }

    private fun checkStoragePermission() {
        mStates.isAllowStorage.set(
            XXPermissions.isGrantedPermissions(
                requireContext(),
                listOf(
                    PermissionLists.getReadMediaImagesPermission(),
                    PermissionLists.getReadMediaVisualUserSelectedPermission(),
                    PermissionLists.getWriteExternalStoragePermission()
                )
            )
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onLocationPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                PermissionLists.getAccessCoarseLocationPermission(),
                PermissionLists.getAccessFineLocationPermission()
            )
        }

        fun onBluetoothPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                PermissionLists.getBluetoothScanPermission(),
                PermissionLists.getBluetoothConnectPermission()
            )
        }

        fun onCameraPermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                PermissionLists.getCameraPermission()
            )
        }

        fun onStoragePermissionClick() {
            XXPermissions.startPermissionActivity(
                this@AppPermissionSettingFragment,
                PermissionLists.getReadMediaImagesPermission(),
                PermissionLists.getReadMediaVisualUserSelectedPermission(),
                PermissionLists.getWriteExternalStoragePermission()
            )
        }
    }
}