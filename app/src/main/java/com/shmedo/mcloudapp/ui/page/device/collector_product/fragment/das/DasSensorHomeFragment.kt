package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasSensorHomeBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BaseBleDasExternalSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BleDasExternalDigitalSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BleDasExternalVibratingSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor.DasExternalSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.DasDigitalOsmometerFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.internalsensor.DasMCUAddressFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

/**
 * @author：gonghe
 * @time: 2025/6/17
 * @desc:物联网采集器(DAS)传感器主页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasSensorHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDasSensorHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    private var mLayoutMediator: TabLayoutMediator? = null

    private var collectorModel = "-1"
    private var productType = ProductType.UnKnown
    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    private var tabNames = arrayListOf<String>()


    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_sensor_home, BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasSensorHomeBinding
        binding.llToolbar.toolbar.title = "传感器配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
        arguments?.let {
            collectorModel = it.getString(COLLECTOR_MODEL, "-1")
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }

        tabNames.clear()
        tabNames.add("开关量")
        tabNames.add("数字式水位计")
        // 4G模式且是MR701产品才显示MCU地址tab
        if (communicateWay == NetPlatformConnect && deviceInfo.productName.contains("MR701")) {
            tabNames.add("MCU地址")
        }
        tabNames.add("扩展传感器")
        initViewPager()
    }

    private fun initViewPager() {
        val fragmentList = mutableListOf<Fragment>()

        if (communicateWay == BleConnect) {
            // 蓝牙模式使用蓝牙相关的Fragment
            val bundle = BaseIOTDeviceFragment.Companion.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )

            fragmentList.add(DasIOSensorFragment.Companion.newInstance().apply {
                arguments = bundle
            })
            fragmentList.add(DasDigitalOsmometerFragment.Companion.newInstance().apply {
                arguments = bundle
            })

            // 根据collectorModel决定使用哪种扩展传感器Fragment
            val externalSensorFragment =
                if (collectorModel == "0${IOTSensorType.VIBRATING_SENSOR.code}")
                    BleDasExternalVibratingSensorListFragment.Companion.newInstance().apply {
                        arguments =
                            BaseBleDasExternalSensorListFragment.Companion.newBundleArguments(
                                collectorModel,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                            )
                    } else
                    BleDasExternalDigitalSensorListFragment.Companion.newInstance().apply {
                        arguments =
                            BaseBleDasExternalSensorListFragment.Companion.newBundleArguments(
                                collectorModel,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                            )
                    }
            fragmentList.add(externalSensorFragment)
        } else {
            // 4G模式使用原有的Fragment
            val bundle = BaseIOTDeviceFragment.Companion.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )

            fragmentList.add(DasIOSensorFragment.Companion.newInstance().apply {
                arguments = bundle
            })
            fragmentList.add(DasDigitalOsmometerFragment.Companion.newInstance().apply {
                arguments = bundle
            })
            if (deviceInfo.productName.contains("MR701")) {
                fragmentList.add(DasMCUAddressFragment.Companion.newInstance().apply {
                    arguments = bundle
                })
            }
            fragmentList.add(DasExternalSensorListFragment.Companion.newInstance().apply {
                arguments = bundle
            })
        }

        binding.viewpager.adapter = PageAdapter(this, fragmentList)
        binding.viewpager.offscreenPageLimit = 1
        binding.viewpager.isUserInputEnabled = false
        binding.tabs.addOnTabSelectedListener(this)
        mLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager, true, false) { tab, position ->
                val tabView =
                    LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_mr702_interface, null)
                val textView =
                    tabView.findViewById<MaterialTextView>(R.id.tabText)
                textView.text = tabNames[position]
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
            }
        mLayoutMediator?.attach()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(activeBg)
            val textView =
                it.findViewById<MaterialTextView>(R.id.tabText)
            textView.textSize = activeSize
            textView.setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(normalBg)
            val textView =
                it.findViewById<MaterialTextView>(R.id.tabText)
            textView.textSize = normalSize
            textView.setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // TabLayout 解绑
        mLayoutMediator?.detach()
        mLayoutMediator = null
    }

    companion object {
        private val activeBg: Int = R.drawable.bg_mr702_port_tab_checked
        private val normalBg: Int = R.drawable.bg_mr702_port_tab_normal
        private val activeColor: Int = ColorUtils.getColor(R.color.white)
        private val normalColor: Int = Color.parseColor("#65A2CD")
        private val activeSize: Float = 15f
        private val normalSize: Float = 15f

        const val COLLECTOR_MODEL = "collector_model"

        fun newBundleArguments(
            collectorModel: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(COLLECTOR_MODEL, collectorModel)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}