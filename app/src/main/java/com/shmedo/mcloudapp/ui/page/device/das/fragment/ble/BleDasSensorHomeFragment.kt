package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble

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
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasSensorHomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BaseBleDasExternalSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BleDasExternalDigitalSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BleDasExternalVibratingSensorListFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.internalsensor.BleDasDigitalOsmometerFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.internalsensor.BleDasIOSensorFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasSensorHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDasSensorHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel

    private var collectorModel = "-1"
    private var productType = ProductType.UnKnown
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
            collectorModel = it.getString(COLLECTOR_MODEL, "-1")
            productType = it.getParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR)
        }
        initViewPager()
    }

    private fun initViewPager() {
        val mFragments =
            listOf<Fragment>(
                BleDasIOSensorFragment.newInstance().apply {
                    arguments = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                },
                BleDasDigitalOsmometerFragment.newInstance().apply {
                    arguments = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                },
                if (collectorModel == "0${IOTSensorType.VIBRATING_SENSOR.code}")
                    BleDasExternalVibratingSensorListFragment.newInstance().apply {
                        arguments = BaseBleDasExternalSensorListFragment.newBundleArguments(
                            collectorModel,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                        )
                    } else
                    BleDasExternalDigitalSensorListFragment.newInstance().apply {
                        arguments = BaseBleDasExternalSensorListFragment.newBundleArguments(
                            collectorModel,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                        )
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
        private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f

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
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}