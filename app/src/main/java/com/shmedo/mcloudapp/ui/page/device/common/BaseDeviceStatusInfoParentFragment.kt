package com.shmedo.mcloudapp.ui.page.device.common

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentBaseDeviceStatusInfoParentBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BaseDeviceStatusInfoParentViewModel

/**
 * 创建者：gonghe
 * 创建时间：2024/4/30
 * 描述： TODO
 */
abstract class BaseDeviceStatusInfoParentFragment : BaseFragment(),
    TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentBaseDeviceStatusInfoParentBinding
    protected lateinit var mStates: BaseDeviceStatusInfoParentViewModel

    protected var productType = ProductType.UnKnown
    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null

    protected val tabs = mutableListOf("基本信息", "通讯状态", "传感器")

    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_base_device_status_info_parent,
            BR.stateVM,
            mStates
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBaseDeviceStatusInfoParentBinding
        binding.toolbar.title = "状态信息"
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

    @CallSuper
    override fun initData() {
        arguments?.let {
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mStates.productName.set(deviceInfo.productName)
        mStates.productType.set("型号：${deviceInfo.deviceName}")
    }

    override fun lazyLoadData() {
        initViewPager()
    }

    private fun initViewPager() {
        val mFragments = initTabFragment()
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = mFragments.size
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

    abstract fun initTabFragment(): List<Fragment>

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)
    }

    override fun lazyLoadTime(): Long {
        return 0
    }

    companion object {
        private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
        private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f
    }
}