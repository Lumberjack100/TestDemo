package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

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
import com.shmedo.mcloudapp.databinding.FragmentMr702DeviceInfoBinding
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.DasSensorHomeFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel

class MR702DeviceInfoFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentMr702DeviceInfoBinding
    private lateinit var mStates: MR702DeviceInfoViewModel

    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_device_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702DeviceInfoBinding
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
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mStates.productName.set(deviceInfo.productName)
        mStates.productType.set("型号：${deviceInfo.deviceName}")
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
                MR702BaseInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RunningStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702PortStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702ModuleStatusInfoFragment.newInstance().apply {
                    arguments = bundle
                }
            )
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = 1
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)

//        mActivity.window.statusBarColor = Color.TRANSPARENT
//        val windowInsetsController = WindowCompat.getInsetsController(mActivity.window, mActivity.window.decorView)
//        windowInsetsController.isAppearanceLightStatusBars = false
//        WindowCompat.setDecorFitsSystemWindows(mActivity.window, false)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets: WindowInsetsCompat ->
//            val systemWindow = insets.getInsets(WindowInsetsCompat.Type.statusBars())
//            val params = view.layoutParams as ConstraintLayout.LayoutParams
//            params.topMargin = systemWindow.top
//
//            insets
//        }
    }

//    override fun onDestroyView() {
//        mActivity.window.statusBarColor = Color.WHITE
//        val windowInsetsController = WindowCompat.getInsetsController(mActivity.window, mActivity.window.decorView)
//        windowInsetsController.isAppearanceLightStatusBars = true
//        WindowCompat.setDecorFitsSystemWindows(mActivity.window, true)
//        super.onDestroyView()
//    }

    companion object {
        private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
        private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f
        private val tabs = arrayOf("基本信息", "运行状态", "接口状态", "模块状态")

        fun newInstance() = DasSensorHomeFragment()
    }
}