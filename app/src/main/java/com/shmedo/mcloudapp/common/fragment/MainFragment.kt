package com.shmedo.mcloudapp.common.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.PermissionInterceptor
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentMainBinding
import com.shmedo.mcloudapp.user.fragment.MineFragment
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel


class MainFragment : BaseFragment() {
    private val binding: FragmentMainBinding by lazy { getBinding() as FragmentMainBinding }
    private val mStates: EmptyViewModel by viewModels()
    private val loginRequestViewModel: LoginRequestViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
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
        requestPermission()
        //加载外部配置
        loginRequestViewModel.loadExternalConfig()
    }

    /**
     * 申请通知权限
     */
    private fun requestPermission() {
        // 申请通知栏权限
        XXPermissions.with(this)
            .permission(Permission.POST_NOTIFICATIONS)
            .interceptor(PermissionInterceptor())
            .request(OnPermissionCallback { permissions, allGranted ->
                if (!allGranted) {
                    return@OnPermissionCallback
                }
            })
    }

}