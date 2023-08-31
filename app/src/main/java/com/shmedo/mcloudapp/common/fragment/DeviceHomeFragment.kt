package com.shmedo.mcloudapp.common.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.viewmodel.state.DeviceHomeViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentDeviceHomeBinding
import com.shmedo.mcloudapp.device.BleScannerListFragment
import com.shmedo.mcloudapp.device.NetDeviceListFragment

class DeviceHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentDeviceHomeBinding by lazy { getBinding() as FragmentDeviceHomeBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: DeviceHomeViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_home, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initViewPager()
    }

    override fun initData() {

    }

    override fun createObserver() {

    }

    private fun initViewPager() {
        val mFragments =
            listOf<Fragment>(
                NetDeviceListFragment.newInstance(),
                BleScannerListFragment.newInstance()
            )
        binding.viewpager.adapter = PageAdapter((mActivity as FragmentActivity), mFragments)
        binding.viewpager.offscreenPageLimit = 1
        binding.viewpager.isUserInputEnabled = false
        val tabLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                if (position == 0) {
                    val tabView = LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_device_home, null)

                    val textView = tabView.findViewById<TextView>(R.id.tabText)
                    textView.text = "4G"
                    textView.textSize = 18f
                    textView.setTextColor(ColorUtils.getColor(R.color.title_text_color))
                    textView.setTypeface(textView.typeface, Typeface.BOLD)
                    tab.customView = tabView
                } else if (position == 1) {
                    val tabView = LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_device_home, null)

                    val textView = tabView.findViewById<TextView>(R.id.tabText)
                    textView.text = "蓝牙"
                    textView.textSize = 14f
                    textView.setTextColor(ColorUtils.getColor(R.color.sub_title_text_color))
                    textView.setTypeface(textView.typeface, Typeface.NORMAL)
                    tab.customView = tabView
                }
            }
        tabLayoutMediator.attach()
        binding.tabs.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = 18f
            textView.setTextColor(ColorUtils.getColor(R.color.title_text_color))
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = 14f
            textView.setTextColor(ColorUtils.getColor(R.color.sub_title_text_color))
            textView.setTypeface(textView.typeface, Typeface.NORMAL)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar, true)
    }

    inner class ClickProxy {

    }


}