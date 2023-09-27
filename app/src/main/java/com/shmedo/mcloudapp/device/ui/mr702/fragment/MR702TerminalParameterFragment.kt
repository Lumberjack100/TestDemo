package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentMr702TerminalParameterBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702TerminalParameterViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   终端参数页面
 */
class MR702TerminalParameterFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702TerminalParameterBinding by lazy { getBinding() as FragmentMr702TerminalParameterBinding }
    private val mStates: MR702TerminalParameterViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_terminal_parameter, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "终端参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
    }

    override fun initData() {
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        initViewPager()
    }

    override fun createObserver() {

    }

    private fun initViewPager() {
        val mFragments = listOf<Fragment>(
            MR702ReportMethodFragment.newInstance().apply {
                arguments = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                    statusBarColor
                )
            },
            MR702ScreenFragment.newInstance().apply {
                arguments = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                    statusBarColor
                )
            }
        )
        binding.viewpager.adapter = PageAdapter((mActivity as FragmentActivity), mFragments)
        binding.viewpager.offscreenPageLimit = 2
        binding.viewpager.isUserInputEnabled = false
        val tabLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                if (position == 0) {
                    val tabView = LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_textview, null)

                    val textView = tabView.findViewById<TextView>(R.id.tabText)
                    textView.text = "上报方式"
                    textView.textSize = 17f
                    textView.setTextColor(ColorUtils.getColor(R.color.colorPrimary))
                    textView.setTypeface(textView.typeface, Typeface.BOLD)
                    tab.customView = tabView
                } else if (position == 1) {
                    val tabView = LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_textview, null)

                    val textView = tabView.findViewById<TextView>(R.id.tabText)
                    textView.text = "本机屏幕"
                    textView.textSize = 15f
                    textView.setTextColor(ColorUtils.getColor(R.color.text_color_666666))
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
            textView.textSize = 17f
            textView.setTextColor(ColorUtils.getColor(R.color.colorPrimary))
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = 15f
            textView.setTextColor(ColorUtils.getColor(R.color.text_color_666666))
            textView.setTypeface(textView.typeface, Typeface.NORMAL)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}