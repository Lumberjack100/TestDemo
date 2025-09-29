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
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor

class OfflineMainFragment : BaseFragment() {
    private lateinit var binding: FragmentOfflineMainBinding
    private val mStates: EmptyViewModel by viewModels()

    private val innerNavController by lazy {
        val host = childFragmentManager.findFragmentById(R.id.inner_nav_host)
                as androidx.navigation.fragment.NavHostFragment
        host.navController
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_offline_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentOfflineMainBinding
        addMenu()
    }

    private fun addMenu() {
        // 顶部菜单 → 设置页
        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_settings) {
                if (innerNavController.currentDestination?.id != R.id.settingFragment) {
                    innerNavController.navigate(R.id.settingFragment)
                }
//                nav().safeNavigate(R.id.action_mainFragment_to_userInfoHomeFragment)

                true
            } else false
        }

        // 根据内层目的地自动更新标题（可选）
        innerNavController.addOnDestinationChangedListener { _, dest, _ ->
            binding.topAppBar.title = dest.label ?: "Main"
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
}