package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinterface

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ResourceUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.adapter.PageAdapter
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentMr702InterfaceHomeBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.MR702InterfaceSensorConfig
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.mr702.fragment.MR702InterfaceSelectionPartShadowPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.MR702InterfaceHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.launch

class MR702InterfaceHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702InterfaceHomeBinding by lazy { getBinding() as FragmentMr702InterfaceHomeBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702InterfaceHomeViewModel by activityViewModels()

    private var mLayoutMediator: TabLayoutMediator? = null

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
    private val tabNames =
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
        loadSensorConfig()
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
        binding.viewpager.offscreenPageLimit = tabNames.size
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    0, 1 -> {
                        mStates.interfaceName.set(tabNames[position] + "Modbus")
                        mStates.interfaceDesc.set("最多支持32支传感器接入")
                    }

                    2 -> {
                        mStates.interfaceName.set(tabNames[position] + "拓展接口")
                        mStates.interfaceDesc.set("最多支持32支传感器接入")
                    }

                    else -> {
                        mStates.interfaceName.set(tabNames[position])
                        mStates.interfaceDesc.set("北斗数传终端")
                    }
                }
            }
        })
        binding.tabs.addOnTabSelectedListener(this)
        mLayoutMediator = TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            val tabView =
                LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_mr702_interface, null)
            val textView = tabView.findViewById<TextView>(R.id.tabText)
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

    private fun loadSensorConfig() {
        lifecycleScope.launch {
            val jsonStr = ResourceUtils.readAssets2String("mr702_interface_sensor_config.json")
            if (jsonStr.isNullOrEmpty()) return@launch
            val configList =
                MoshiUtil.fromJson<List<MR702InterfaceSensorConfig>>(jsonStr) ?: return@launch
            configList.forEach { mInterface ->
                mStates.sensorConfig[mInterface.interfaceName] = mInterface.models
                mInterface.models.forEach { model ->
                    mStates.sensorModelMap[model.modelToken] = model
                }
            }
        }
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
        mStates.isEditable.value = editable
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        fun onShowSelectInterfacePopup() {
            val selectionPopupView = MR702InterfaceSelectionPartShadowPopupView(requireContext())
            selectionPopupView.setData(tabNames.toList(), binding.viewpager.currentItem)
                .setSelectListener(object :
                    MR702InterfaceSelectionPartShadowPopupView.OnSelectListener {
                    override fun onSelect(name: String) {
                        tabNames.indexOfFirst { it == name }.let {
                            binding.viewpager.setCurrentItem(it, false)
                        }
                    }
                })
            XPopup.Builder(context)
                .atView(binding.headLine)
                .isViewMode(true)
                .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
                .dismissOnTouchOutside(true)// 点击外部是否关闭弹窗，默认为true
                .enableDrag(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asCustom(selectionPopupView)
                .show()
        }
    }

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
}