package com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor

import android.os.Bundle
import android.view.View
import com.amap.api.col.`3l`.it
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentDasExternalVibratingSensorBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalVibratingSensorViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Arrays
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/2/5
 * @desc: 振弦传感器
 *
 */
class DasExternalVibratingSensorFragment : BaseFragment() {
    private lateinit var binding: FragmentDasExternalVibratingSensorBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasExternalVibratingSensorViewModel
    private lateinit var sensorListViewModel: DasExternalSensorListViewModel

    private val sensorTypeList = listOf(
        IOTSensorType.KANG_PERCOLATE.description,
        IOTSensorType.GUDAN_PERCOLATE.description,
        IOTSensorType.JUNXING_ZLJ_300T.description,
        IOTSensorType.GUDAN_STRESS.description,
        MCU_PREFIX + IOTSensorType.VW08.description,
        MCU_PREFIX + IOTSensorType.WEIR.description,
        MCU_PREFIX + IOTSensorType.WATER_LEVEL_GAUGE.description
    )
    private val iotSensorType: IOTSensorType by lazy {
        IOTSensorType.getSensorTypeByCollectorCode(sensorListViewModel.collectorType.get())
    }

    private val allChannelList: List<String> = mutableListOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16"
    ) //所有通道
    private val unUsedChannelList = ArrayList<String>()

    private var sensorChannel = "-1"
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        sensorListViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_external_vibrating_sensor,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasExternalVibratingSensorBinding
        binding.llToolbar.toolbar.title = "振弦式传感器"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            sensorChannel = it.getString(SENSOR_CHANNEL, "-1")
        }
        unUsedChannelList.clear()
        unUsedChannelList.addAll(allChannelList)
        val usedList = sensorListViewModel.sensorModelMap.keys.filterNot { it == sensorChannel }
            .map { (it.toInt() + 1).toString() }
        unUsedChannelList.removeAll(usedList.toSet())

        val externalSensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorChannel))
            sensorListViewModel.sensorModelMap[sensorChannel]!!
        else {
            DasExternalSensorInfo(
                addr = "",
                type = iotSensorType.code,
                threshold = "",
                corrval = ""
            )
        }
        initSensorInfo(externalSensorInfo)
    }

    private fun initSensorInfo(sensorInfo: DasExternalSensorInfo) {

    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 传感器类型选择
         */
        fun onSensorSwitchClick() {
            val selectedIndex = modelTypeList.indexOf(mStates.modelType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modelTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.modelType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onChannelSwitchClick() {
            val selectedIndex = channelList.indexOf(mStates.channel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", channelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.channel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            checkValueIsValidAndUpdateSensor()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val MCU_PREFIX = "MCU_"
        private const val SENSOR_CHANNEL = "sensor_channel"

        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            sensorChannel: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putString(SENSOR_CHANNEL, sensorChannel)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}