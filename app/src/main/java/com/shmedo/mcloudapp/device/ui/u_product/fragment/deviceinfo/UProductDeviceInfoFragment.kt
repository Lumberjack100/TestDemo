package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

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
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentM20DeviceInfoBinding
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.M20DeviceInfoViewModel
import com.shmedo.mcloudapp.ext.nav

/**
 * 创建者：gonghe
 * 创建时间：2024/4/25
 * 描述： TODO
 */
class UProductDeviceInfoFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentM20DeviceInfoBinding
    private lateinit var mStates: M20DeviceInfoViewModel
    private var productType = ProductType.UnKnown
    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_device_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20DeviceInfoBinding
        binding.toolbar.title = "设备状态"
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
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mStates.productName.set(deviceInfo.productName)
        mStates.productType.set("型号：${deviceInfo.deviceName}")
//        mStates.productType.set(if (deviceInfo.deviceToken.endsWith(ProductType.GNSS_M_1.newSuffix)) "型号：M20 (单北斗)" else "型号：M20 (全星座)")
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
                UProductBaseInfoFragment.newInstance().apply {
                    arguments = bundle
                },
                UProductCommunicationInfoFragment.newInstance().apply {
                    arguments = UProductCommunicationInfoFragment.newBundleArguments(
                        3,
                        communicateWay,
                        deviceInfo,
                        bleDevice,
                        statusBarColor
                    )
                },
                when (productType) {
                    ProductType.U_D_1,//水位
                    ProductType.U_D_2 -> {//泥位
                        UDProductSensorInfoFragment.newInstance().apply {
                            arguments = bundle
                        }
                    }

                    ProductType.U_I_1 -> {//倾斜仪
                        UIProductSensorInfoFragment.newInstance().apply {
                            arguments = bundle
                        }
                    }

                    ProductType.U_R_1 -> {//雨量计
                        URProductSensorInfoFragment.newInstance().apply {
                            arguments = bundle
                        }
                    }

                    else -> URProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }

            )
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)
    }

    companion object {
        private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
        private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f
        private val tabs = arrayOf("基本信息", "通讯状态", "传感器")
    }
}