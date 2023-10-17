package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinterface

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
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
import com.shmedo.mcloudapp.databinding.FragmentMr702InterfaceHomeBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702InterfaceHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel

class MR702InterfaceHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702InterfaceHomeBinding by lazy { getBinding() as FragmentMr702InterfaceHomeBinding }
    private val mStates: MR702InterfaceHomeViewModel by activityViewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    private val activeBg: Int = R.drawable.bg_mr702_interface_tab_checked
    private val normalBg: Int = R.drawable.bg_mr702_interface_tab_normal
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int = Color.parseColor("#65A2CD")
    private val activeSize: Float = 15f
    private val normalSize: Float = 15f
    private val tabs =
        arrayOf("RS485-1", "RS485-2", "RS485-3", "RS232-1", "RS232-2", "雨量", "DO", "DI")

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_interface_home, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "接口配置"
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
        mStates.interfaceName.set("RS485-1 Modbus")
        mStates.interfaceDesc.set("最多支持32支传感器接入")
        initViewPager()
    }

    private fun initViewPager() {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val mFragments = listOf<Fragment>(
            MR702RS4851InterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RS4852InterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RS4853InterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RS2321InterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RS2322InterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RainInterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702DOInterfaceFragment.newInstance().apply {
                arguments = bundle
            },
            MR702DIInterfaceFragment.newInstance().apply {
                arguments = bundle
            },
        )
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewpager.adapter = PageAdapter((mActivity as FragmentActivity), mFragments)
        binding.viewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    0, 1 -> {
                        mStates.interfaceName.set(tabs[position] + "Modbus")
                        mStates.interfaceDesc.set("最多支持32支传感器接入")
                    }

                    2 -> {
                        mStates.interfaceName.set(tabs[position] + "拓展接口")
                        mStates.interfaceDesc.set("最多支持32支传感器接入")
                    }

                    else -> {
                        mStates.interfaceName.set(tabs[position])
                        mStates.interfaceDesc.set("北斗数传终端")
                    }
                }
            }
        })
        binding.tabs.addOnTabSelectedListener(this)

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            val tabView =
                LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_mr702_interface, null)
            val textView = tabView.findViewById<TextView>(R.id.tabText)
            textView.text = tabs[position]
            if (position == 0) { // 第一个为默认选中
                tabView.setBackgroundResource(activeBg)
                textView.textSize = activeSize
                textView.setTextColor(activeColor)
            } else {
                tabView.setBackgroundResource(normalBg)
                textView.textSize = normalSize
                textView.setTextColor(normalColor)
            }
            tab.customView = tabView
        }.attach()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(activeBg)
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = activeSize
            textView.setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(normalBg)
            val textView = it.findViewById<TextView>(R.id.tabText)
            textView.textSize = normalSize
            textView.setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}