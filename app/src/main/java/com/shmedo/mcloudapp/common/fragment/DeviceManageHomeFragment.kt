package com.shmedo.mcloudapp.common.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.databinding.FragmentDeviceManageHomeBinding
import com.shmedo.mcloudapp.device.ui.BleScannerListFragment
import com.shmedo.mcloudapp.device.ui.NetDeviceListFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DeviceManageHomeViewModel

class DeviceManageHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDeviceManageHomeBinding
    private lateinit var mStates: DeviceManageHomeViewModel
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private val activeColor: Int = ColorUtils.getColor(R.color.title_text_color)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 18f
    private val normalSize: Float = 16f
    private val tabs = arrayOf("4G", "蓝牙")


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_manage_home, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceManageHomeBinding
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
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = mFragments.size
        binding.viewpager.isUserInputEnabled = false
        binding.tabs.addOnTabSelectedListener(this)

        val tabLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                val textView = TextView(requireContext())

                textView.text = tabs[position]
                if (position == 0) { // 第一个为默认选中
                    textView.textSize = activeSize
                    textView.typeface = Typeface.DEFAULT_BOLD
                    textView.gravity = Gravity.CENTER
                    textView.setTextColor(activeColor)
                } else {
                    textView.textSize = normalSize
                    textView.typeface = Typeface.DEFAULT
                    textView.gravity = Gravity.CENTER
                    textView.setTextColor(normalColor)
                }
                tab.customView = textView
            }
        tabLayoutMediator.attach()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = activeSize
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = normalSize
            typeface = Typeface.DEFAULT
            setTextColor(normalColor)
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