package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.core.model.AppConfigInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702PortHomeBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.AppConfigContent
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.adapter.PageAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.MR702PortSelectionPartShadowPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

class MR702PortHomeFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentMr702PortHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702PortHomeViewModel
    private lateinit var logViewModel: LogViewModel

    private var mLayoutMediator: TabLayoutMediator? = null

    private var productType = ProductType.UnKnown
    private var statusBarColor = 0
    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    private val activeBg: Int = R.drawable.bg_mr702_port_tab_checked
    private val normalBg: Int = R.drawable.bg_mr702_port_tab_normal
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int = Color.parseColor("#65A2CD")
    private val activeSize: Float = 15f
    private val normalSize: Float = 15f
    private val tabNames =
        arrayOf("RS485-1", "RS485-2", "RS485-3", "RS232-1", "RS232-2", "雨量", "开关量")


    override fun initViewModel() {
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getActivityScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_port_home, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702PortHomeBinding
        binding.llToolbar.toolbar.title = "接口配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nav().navigateUp()
            }
        })
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
    }

    override fun initData() {
        loadSensorModeConfig()
        arguments?.let {
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
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
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val mFragments =
            listOf<Fragment>(
                MR702RS485Port1Fragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RS485Port2Fragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RS485Port3Fragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RS232Port1Fragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RS232Port2Fragment.newInstance().apply {
                    arguments = bundle
                },
                MR702RainPortFragment.newInstance().apply {
                    arguments = bundle
                },
                MR702IOPortFragment.newInstance().apply {
                    arguments = bundle
                }
            )
        binding.viewpager.adapter = PageAdapter(this, mFragments)
        binding.viewpager.offscreenPageLimit = 1
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    0 -> {
                        mStates.interfaceName.set(tabNames[position] + "Modbus")
                        mStates.interfaceDesc.set("最多支持32支传感器接入")
                    }

                    1 -> {
                        mStates.interfaceName.set(tabNames[position] + "非Modbus")
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
        mLayoutMediator =
            TabLayoutMediator(binding.tabs, binding.viewpager, true, false) { tab, position ->
                val tabView =
                    LayoutInflater.from(mActivity)
                        .inflate(R.layout.custom_tab_mr702_interface, null)
                val textView =
                    tabView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
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

    private fun loadSensorModeConfig() {
        launchWithViewLifecycle(Dispatchers.IO) {
            try {
                val localAppConfigInfo: AppConfigInfo = MmkvCacheUtil.getAppConfigInfo()!!
                val jsonStr = localAppConfigInfo.configPara.replace("\\", "")
                //Timber.d("configPara = $jsonStr")
                val appConfigContent: AppConfigContent =
                    MoshiUtil.fromJson(jsonStr) ?: return@launchWithViewLifecycle

                appConfigContent.mr702.forEach { mPort ->
                    mStates.configPortSensorModelListMap[mPort.portName] =
                        mPort.sensorModelList.toMutableList()
                }
                mStates.configPortSensorModelListMap["485port1"]?.let { modelList ->
                    modelList.onEachIndexed { index, sensorModel ->
                        mStates.configPort4851SensorNameToSensorModelMap[sensorModel.sensorName] =
                            sensorModel
                        mStates.modelTokenToSensorModelMap[sensorModel.modelToken] = sensorModel
                    }
                }
                mStates.configPortSensorModelListMap["485port2"]?.let { modelList ->
                    modelList.onEachIndexed { index, sensorModel ->
                        mStates.configPort4852SensorTypeToSensorModelMap[sensorModel.sensorType] =
                            sensorModel
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(activeBg)
            val textView =
                it.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
            textView.textSize = activeSize
            textView.setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            it.setBackgroundResource(normalBg)
            val textView =
                it.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tabText)
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
            val selectionPopupView = MR702PortSelectionPartShadowPopupView(requireContext())
            selectionPopupView.setData(tabNames.toList(), binding.viewpager.currentItem)
                .setSelectListener(object :
                    MR702PortSelectionPartShadowPopupView.OnSelectListener {
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

    fun addDeviceLogItem(priority: Int, data: String) {
        logViewModel.insertLogItem(
            getLogItem(
                sessionId = CommonMMKVOwner.iotDeviceLogSessionId,
                priority = priority,
                data = data
            )
        )
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