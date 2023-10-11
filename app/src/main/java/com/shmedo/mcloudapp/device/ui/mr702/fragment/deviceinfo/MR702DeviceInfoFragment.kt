package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.databinding.FragmentMr702DeviceInfoBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel

class MR702DeviceInfoFragment : BaseIOTDeviceFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702DeviceInfoBinding by lazy { getBinding() as FragmentMr702DeviceInfoBinding }
    private val mStates: MR702DeviceInfoViewModel by viewModels()

    private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 17f
    private val normalSize: Float = 15f
    private val tabs = arrayOf("基本信息", "运行状态", "接口状态", "模块状态")

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_device_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.toolbar.title = "关于设备"
        binding.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
        super.initData()
        initViewPager()
    }

    private fun initViewPager() {
        val bundle = newBundleArguments(
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val mFragments =
            listOf<Fragment>(
                MR702BaseInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RunningStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702InterfaceStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702ModuleStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                }
            )
        binding.viewpager.adapter = PageAdapter((mActivity as FragmentActivity), mFragments)
        binding.viewpager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewpager.isUserInputEnabled = false
        binding.tabs.addOnTabSelectedListener(this)

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
        }.attach()
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

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult()
    }

    override fun setResultData(cmdStr: String) {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)
    }
}