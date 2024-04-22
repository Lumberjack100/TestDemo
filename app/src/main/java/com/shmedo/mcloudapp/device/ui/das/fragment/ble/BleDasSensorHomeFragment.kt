package com.shmedo.mcloudapp.device.ui.das.fragment.ble

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
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentDasSensorHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor.BaseBleDasExternalSensorListFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor.BleDasExternalDigitalSensorListFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor.BleDasExternalVibratingSensorListFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.internalsensor.BleDasDigitalOsmometerFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.internalsensor.BleDasIOSensorFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasSensorHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDasSensorHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel

    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null
    private var collectorModel = "-1"

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
            collectorModel = it.getString(COLLECTOR_MODEL, "-1")
        }
        initViewPager()
    }

    private fun initViewPager() {
        val mFragments =
            listOf<Fragment>(
                BleDasIOSensorFragment.newInstance().apply {
                    arguments = BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                },
                BleDasDigitalOsmometerFragment.newInstance().apply {
                    arguments = BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                },
                if (collectorModel == "0${IOTSensorType.VIBRATING_SENSOR.code}")
                    BleDasExternalVibratingSensorListFragment.newInstance().apply {
                        arguments = BaseBleDasExternalSensorListFragment.newBundleArguments(
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            collectorModel
                        )
                    } else
                    BleDasExternalDigitalSensorListFragment.newInstance().apply {
                        arguments = BaseBleDasExternalSensorListFragment.newBundleArguments(
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            collectorModel
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
        private val normalColor: Int = ColorUtils.getColor(R.color.title_text_color)
        private const val activeSize: Float = 17f
        private const val normalSize: Float = 15f

        private const val COLLECTOR_MODEL = "collector_model"
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            collectorModel: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putString(COLLECTOR_MODEL, collectorModel)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}