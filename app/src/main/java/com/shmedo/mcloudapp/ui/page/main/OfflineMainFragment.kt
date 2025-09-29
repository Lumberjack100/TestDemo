package com.shmedo.mcloudapp.ui.page.main

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentOfflineMainBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BleScannerListFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor

class OfflineMainFragment : BaseFragment() {
    private lateinit var binding: FragmentOfflineMainBinding
    private val mStates: EmptyViewModel by viewModels()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_offline_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentOfflineMainBinding
        addMenu()

        if (childFragmentManager.findFragmentByTag("child") == null) {
            val child = BleScannerListFragment.newInstance() // 你的子 Fragment
            childFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.childFragmentContainer, child, "child")
                .commit()
        }
    }

    private fun addMenu() {
        // 顶部菜单 → 设置页
        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_settings) {
                nav().safeNavigate(R.id.action_global_to_settingFragment)

                true
            } else false
        }
    }

    override fun lazyLoadData() {
        requestPermission()
    }

    /**
     * 申请通知权限
     */
    private fun requestPermission() {
        // 申请通知栏权限
        XXPermissions.with(this)
            .permission(PermissionLists.getPostNotificationsPermission())
            // 设置权限请求拦截器（局部设置）
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(OnPermissionCallback { grantedList, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (!allGranted) {
                    return@OnPermissionCallback
                }
            })
    }

    override fun onResume() {
        super.onResume()
        binding.root.post {
            initImmersionBar(binding.topAppBar)
        }
    }
}