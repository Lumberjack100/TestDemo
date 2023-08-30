package com.shmedo.mcloudapp.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.navigation.NavigationBarView

import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentMainBinding


class MainFragment : BaseFragment() {

    private val binding: FragmentMainBinding by lazy { getBinding() as FragmentMainBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: EmptyViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_main, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val viewPagerAdapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if(position == 0) HomeFragment() else MineFragment(
            }

            override fun getItemCount() = 2
        }
        binding.mainViewpager.isUserInputEnabled = false
        binding.mainViewpager.offscreenPageLimit = 2
        binding.mainViewpager.adapter = viewPagerAdapter
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
//        mMessenger.bottomTabCurrentItem.observe(viewLifecycleOwner) {
//            when (it) {
//                R.id.item_config_module -> {
//                    binding.mainViewpager.setCurrentItem(0, false)
//                }
//
//                R.id.item_me_module -> {
//                    binding.mainViewpager.setCurrentItem(1, false)
//                }
//            }
//        }
    }

}