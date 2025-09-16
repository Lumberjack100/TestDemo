package com.shmedo.mcloudapp.ui.page.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMainBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.userprofile.MineFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.AppUpdateViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.ProductConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : BaseFragment() {
    private lateinit var binding: FragmentMainBinding
    private val mStates: EmptyViewModel by viewModels()
    private val appUpdateViewModel: AppUpdateViewModel by viewModel()
    private val productConfigViewModel: ProductConfigViewModel by viewModel()


    override fun initViewModel() {

    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMainBinding
        val mFragments =
            listOf<Fragment>(
                DeviceManageHomeFragment(),
                MineFragment()
            )
        binding.mainViewpager.adapter = PageAdapter(this, mFragments)
        binding.mainViewpager.offscreenPageLimit = mFragments.size
        binding.mainViewpager.isUserInputEnabled = false
        binding.mainViewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.mainBottom.menu.findItem(R.id.item_config_module).isChecked = true
                    }

                    1 -> {
                        binding.mainBottom.menu.findItem(R.id.item_me_module).isChecked = true
                    }
                }
            }
        })
        binding.mainBottom.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_config_module -> {
                    binding.mainViewpager.setCurrentItem(0, false)
                    return@OnItemSelectedListener true
                }

                R.id.item_me_module -> {
                    binding.mainViewpager.setCurrentItem(1, false)
                    return@OnItemSelectedListener true
                }
            }
            false
        }

    override fun createObserver() {

    }

    override fun lazyLoadData() {
        //加载外部配置
        productConfigViewModel.loadMR702SensorConfig()
        requestPermission()
        launchWithViewLifecycle {
            delay(1500)
            appUpdateViewModel.requestCheckAppVersion(false)
        }
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