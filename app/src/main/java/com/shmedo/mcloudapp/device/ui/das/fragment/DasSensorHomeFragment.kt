package com.shmedo.mcloudapp.device.ui.das.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentDasSensorHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor.DasExternalSensorListFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.internalsensor.DasDigitalOsmometerFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.internalsensor.DasIOSensorFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.internalsensor.DasMCUAddressFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel

class DasSensorHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDasSensorHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel

    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    private val tabNames = arrayListOf<String>("开关量", "数字式水位计", "扩展传感器")


    override fun initViewModel() {
        toolbarViewModel = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_sensor_home, BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasSensorHomeBinding
        binding.llToolbar.toolbar.title = "传感器配置"
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
    }

    override fun initData() {
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        if (deviceInfo.productName.contains("MR701")) {
            tabNames.add(2, "MCU地址")
        }
        initViewPager()
    }

    private fun initViewPager() {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val mFragments =
            listOf<Fragment>(
                DasIOSensorFragment.newInstance().apply {
                    arguments = bundle
                },
                DasDigitalOsmometerFragment.newInstance().apply {
                    arguments = bundle
                },
                DasMCUAddressFragment.newInstance().apply {
                    arguments = bundle
                },
                DasExternalSensorListFragment.newInstance().apply {
                    arguments = bundle
                }
            )
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = tabNames.size
        binding.viewpager.isUserInputEnabled = false
        binding.tabs.addOnTabSelectedListener(this)

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            val textView = TextView(requireContext())

            textView.text = tabNames[position]
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
        private val normalColor: Int = ColorUtils.getColor(R.color.title_text_color)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f
        fun newInstance() = DasSensorHomeFragment()
    }
}