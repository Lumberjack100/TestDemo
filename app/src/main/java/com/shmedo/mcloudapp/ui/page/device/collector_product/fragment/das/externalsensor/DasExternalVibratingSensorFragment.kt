package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseDasExternalVibratingSensorClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasExternalVibratingSensorBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalVibratingSensorViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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
    private lateinit var sensorListViewModel: DasExternalSensorListViewModel<DasExternalSensorInfo>

    private val sensorTypeList = listOf(
        IOTSensorType.KANG_PERCOLATE.description,
        IOTSensorType.GUDAN_PERCOLATE.description,
        IOTSensorType.JUNXING_ZLJ_300T.description,
        IOTSensorType.GUDAN_STRESS.description,
        MCU_PREFIX + IOTSensorType.VIBRATING_SENSOR.description,
        MCU_PREFIX + IOTSensorType.WEIR.description,
        MCU_PREFIX + IOTSensorType.WATER_LEVEL_GAUGE.description,
        IOTSensorType.GENERAL_STRING_INSTRUMENT.description
    )
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
    private lateinit var sensorInfo: DasExternalSensorInfo

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
            sensorChannel = it.getString(AppContants.Extras.SENSOR_CHANNEL, "-1")
        }
        unUsedChannelList.clear()
        unUsedChannelList.addAll(allChannelList)
        val usedList = sensorListViewModel.sensorModelMap.keys.filterNot { it == sensorChannel }
            .map { (it.toInt() + 1).toString() }
        unUsedChannelList.removeAll(usedList.toSet())

        if (sensorChannel == "-1") {
            mStates.channel.set(unUsedChannelList[0])
        } else {
            mStates.channel.set((sensorChannel.toInt() + 1).toString())
        }

        sensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorChannel))
            sensorListViewModel.sensorModelMap[sensorChannel]!!
        else {
            DasExternalSensorInfo(
                addr = "",
                type = if (sensorListViewModel.sensorModelMap.isEmpty()) sensorListViewModel.collectorType.get()
                else sensorListViewModel.sensorModelMap.values.first().type,
                threshold = "",
                corrval = ""
            )
        }
        mStates.sensorType.set(IOTSensorType.Companion.value(sensorInfo.type))
        mStates.sensorTypeName.set(IOTSensorType.Companion.value(sensorInfo.type).description)
        switchSensorType()
    }

    private fun switchSensorType() {
        try {
            when (mStates.sensorType.get()) {
                IOTSensorType.KANG_PERCOLATE //基康渗压计(BGK-4500)
                -> {
                    mStates.sensorTypeName.set(IOTSensorType.KANG_PERCOLATE.description)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("触发值（毫米）")
                    decimalFormat.applyPattern("0")
                    sensorInfo.threshold.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("修正值（米）")
                    decimalFormat.applyPattern("#.###")
                    mStates.extension2Value.set("0")//默认值 0
                    sensorInfo.corrval.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.extension3Title.set("多项式系数A")
                    mStates.extension3Value.set(
                        sensorInfo.poly_a.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension4Support.set(true)
                    mStates.extension4Title.set("多项式系数B")
                    mStates.extension4Value.set(
                        sensorInfo.poly_b.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension5Support.set(true)
                    mStates.extension5Title.set("多项式系数C")
                    mStates.extension5Value.set(
                        sensorInfo.poly_c.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension6Support.set(true)
                    mStates.extension6Title.set("温度系数K")
                    mStates.extension6Value.set(
                        sensorInfo.temp_k.toDoubleOrNull()?.toString() ?: ""
                    )

                    //初始温度，精确到小数点后两位
                    mStates.isExtension7Support.set(true)
                    mStates.extension7Title.set("初始温度T0（摄氏度）")
                    mStates.extension7Value.set("0")//默认值 0
                    decimalFormat.applyPattern("#.##")
                    sensorInfo.temp_t0.toDoubleOrNull()?.let {
                        mStates.extension7Value.set(decimalFormat.format(it))
                    }

                    //绳长，精确到小数点后三位
                    mStates.isExtension8Support.set(true)
                    mStates.extension8Title.set("绳长（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.ropelen.toDoubleOrNull()?.let {
                        mStates.extension8Value.set(decimalFormat.format(it))
                    }

                    //安装高程，精确到小数点后三位
                    mStates.isExtension9Support.set(true)
                    mStates.extension9Title.set("安装高程（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.tubealti.toDoubleOrNull()?.let {
                        mStates.extension9Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.GUDAN_PERCOLATE //葛南渗压计(VWP-03)
                -> {
                    mStates.sensorTypeName.set(IOTSensorType.GUDAN_PERCOLATE.description)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("触发值（毫米）")
                    decimalFormat.applyPattern("0")
                    sensorInfo.threshold.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("修正值（米）")
                    decimalFormat.applyPattern("#.###")
                    mStates.extension2Value.set("0")//默认值 0
                    sensorInfo.corrval.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.extension3Title.set("灵敏度K")
                    mStates.extension3Value.set(
                        sensorInfo.sens_k.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension4Support.set(true)
                    mStates.extension4Title.set("温修系数B")
                    mStates.extension4Value.set(
                        sensorInfo.temp_b.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension5Support.set(true)
                    mStates.extension5Title.set("基准值F0")
                    mStates.extension5Value.set(
                        sensorInfo.referval_f.toDoubleOrNull()?.toString() ?: "0"
                    )

                    //初始温度，精确到小数点后两位
                    mStates.isExtension6Support.set(true)
                    mStates.extension6Title.set("初始温度T0（摄氏度）")
                    mStates.extension6Value.set("0")//默认值 0
                    decimalFormat.applyPattern("#.##")
                    sensorInfo.temp_t0.toDoubleOrNull()?.let {
                        mStates.extension6Value.set(decimalFormat.format(it))
                    }

                    //绳长，精确到小数点后三位
                    mStates.isExtension7Support.set(true)
                    mStates.extension7Title.set("绳长（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.ropelen.toDoubleOrNull()?.let {
                        mStates.extension7Value.set(decimalFormat.format(it))
                    }

                    //安装高程，精确到小数点后三位
                    mStates.isExtension8Support.set(true)
                    mStates.extension8Title.set("安装高程（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.tubealti.toDoubleOrNull()?.let {
                        mStates.extension8Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.JUNXING_ZLJ_300T //轴力计(ZLJ-300T)
                -> {
                    mStates.sensorTypeName.set(IOTSensorType.JUNXING_ZLJ_300T.description)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("触发值（千牛）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.threshold.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("修正值（千牛）")
                    decimalFormat.applyPattern("#.###")
                    mStates.extension2Value.set("0")//默认值 0
                    sensorInfo.corrval.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.extension3Title.set("灵敏度K")
                    mStates.extension3Value.set(
                        sensorInfo.sens_k.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension4Support.set(true)
                    mStates.extension4Title.set("温修系数B")
                    mStates.extension4Value.set(
                        sensorInfo.temp_b.toDoubleOrNull()?.toString() ?: "0"
                    )

                    mStates.isExtension5Support.set(true)
                    mStates.extension5Title.set("基准值F0")
                    mStates.extension5Value.set(
                        sensorInfo.referval_f.toDoubleOrNull()?.toString() ?: "0"
                    )

                    //初始温度，精确到小数点后两位
                    mStates.isExtension6Support.set(true)
                    mStates.extension6Title.set("初始温度T0（摄氏度）")
                    mStates.extension6Value.set("0")//默认值 0
                    decimalFormat.applyPattern("#.##")
                    sensorInfo.temp_t0.toDoubleOrNull()?.let {
                        mStates.extension6Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension7Support.set(false)
                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.GUDAN_STRESS //应力计
                -> {
                    mStates.sensorTypeName.set(IOTSensorType.GUDAN_STRESS.description)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("触发值（千牛）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.threshold.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("修正值（千牛）")
                    decimalFormat.applyPattern("#.###")
                    mStates.extension2Value.set("0")//默认值 0
                    sensorInfo.corrval.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.extension3Title.set("灵敏度K")
                    mStates.extension3Value.set(
                        sensorInfo.sens_k.toDoubleOrNull()?.toString() ?: ""
                    )

                    mStates.isExtension4Support.set(true)
                    mStates.extension4Title.set("温修系数B")
                    mStates.extension4Value.set(
                        sensorInfo.temp_b.toDoubleOrNull()?.toString() ?: "0"
                    )

                    mStates.isExtension5Support.set(true)
                    mStates.extension5Title.set("基准值F0")
                    mStates.extension5Value.set(
                        sensorInfo.referval_f.toDoubleOrNull()?.toString() ?: "0"
                    )

                    //初始温度，精确到小数点后两位
                    mStates.isExtension6Support.set(true)
                    mStates.extension6Title.set("初始温度T0（摄氏度）")
                    mStates.extension6Value.set("0")//默认值 0
                    decimalFormat.applyPattern("#.##")
                    sensorInfo.temp_t0.toDoubleOrNull()?.let {
                        mStates.extension6Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension7Support.set(true)
                    mStates.extension7Title.set("膨胀系数")
                    mStates.extension7Value.set(sensorInfo.elastic_mod)

                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.VIBRATING_SENSOR//MCU_振弦传感器
                -> {
                    mStates.sensorTypeName.set(MCU_PREFIX + IOTSensorType.VIBRATING_SENSOR.description)

                    if (sensorInfo.sens_k != IOTConstants.NULL_KEY) {
                        mStates.isExtension1Support.set(true)
                        mStates.extension1Title.set("灵敏度K")
                        mStates.extension1Value.set(
                            sensorInfo.sens_k.toDoubleOrNull()?.toString() ?: ""
                        )

                        mStates.isExtension2Support.set(true)
                        mStates.extension2Title.set("温修系数B")
                        mStates.extension2Value.set(
                            sensorInfo.temp_b.toDoubleOrNull()?.toString() ?: "0"
                        )

                        mStates.isExtension3Support.set(true)
                        mStates.extension3Title.set("基准值F0")
                        mStates.extension3Value.set(
                            sensorInfo.referval_f.toDoubleOrNull()?.toString() ?: "0"
                        )

                        //初始温度，精确到小数点后两位
                        mStates.isExtension4Support.set(true)
                        mStates.extension4Title.set("初始温度T0（摄氏度）")
                        mStates.extension4Value.set("0")//默认值 0
                        decimalFormat.applyPattern("#.##")
                        sensorInfo.temp_t0.toDoubleOrNull()?.let {
                            mStates.extension4Value.set(decimalFormat.format(it))
                        }
                    } else {
                        mStates.isExtension1Support.set(false)
                        mStates.isExtension2Support.set(false)
                        mStates.isExtension3Support.set(false)
                        mStates.isExtension4Support.set(false)
                    }

                    mStates.isExtension5Support.set(sensorInfo.caddr != IOTConstants.NULL_KEY)
                    mStates.extension5Title.set("测站编码")
                    mStates.extension5Value.set(if (mStates.isExtension5Support.get()) sensorInfo.caddr else IOTConstants.NULL_KEY)

                    //mStates.isExtension5Support.set(false)
                    mStates.isExtension6Support.set(false)
                    mStates.isExtension7Support.set(false)
                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.WEIR//MCU_量水堰计
                -> {
                    mStates.sensorTypeName.set(MCU_PREFIX + IOTSensorType.WEIR.description)

                    mStates.isExtension3Support.set(sensorInfo.caddr != IOTConstants.NULL_KEY)
                    mStates.extension3Title.set("测站编码")
                    mStates.extension3Value.set(if (mStates.isExtension3Support.get()) sensorInfo.caddr else IOTConstants.NULL_KEY)

                    mStates.isExtension1Support.set(false)
                    mStates.isExtension2Support.set(false)
                    //mStates.isExtension3Support.set(false)
                    mStates.isExtension4Support.set(false)
                    mStates.isExtension5Support.set(false)
                    mStates.isExtension6Support.set(false)
                    mStates.isExtension7Support.set(false)
                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.WATER_LEVEL_GAUGE//MCU 水位(液位)计
                -> {
                    mStates.sensorTypeName.set(MCU_PREFIX + IOTSensorType.WATER_LEVEL_GAUGE.description)
                    //修正值、绳长、安装高程
                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("修正值（毫米）")
                    decimalFormat.applyPattern("0")
                    mStates.extension1Value.set("0")//默认值 0
                    sensorInfo.corrval.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("绳长（毫米）")
                    decimalFormat.applyPattern("0")
                    sensorInfo.ropelen.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.extension3Title.set("安装高程（毫米）")
                    decimalFormat.applyPattern("0")
                    sensorInfo.tubealti.toDoubleOrNull()?.let {
                        mStates.extension3Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension4Support.set(false)
                    mStates.isExtension5Support.set(false)
                    mStates.isExtension6Support.set(false)
                    mStates.isExtension7Support.set(false)
                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                IOTSensorType.GENERAL_STRING_INSTRUMENT//通用弦式仪
                    -> {
                    mStates.sensorTypeName.set(IOTSensorType.GENERAL_STRING_INSTRUMENT.description)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("触发值")
                    decimalFormat.applyPattern("0")
                    sensorInfo.threshold.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(false)
                    mStates.isExtension3Support.set(false)
                    mStates.isExtension4Support.set(false)
                    mStates.isExtension5Support.set(false)
                    mStates.isExtension6Support.set(false)
                    mStates.isExtension7Support.set(false)
                    mStates.isExtension8Support.set(false)
                    mStates.isExtension9Support.set(false)
                    mStates.isExtension10Support.set(false)
                }

                else -> {}
            }
        } catch (e: Exception) {
            Timber.Forest.e(e)
        }
    }

    inner class ClickProxy : BaseDasExternalVibratingSensorClickProxy() {
        /**
         * 传感器类型选择
         */
        override fun onSensorSwitchClick() {
            val selectedIndex = sensorTypeList.indexOf(mStates.sensorTypeName.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", sensorTypeList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.sensorTypeName.set(text)
                        mStates.sensorType.set(
                            IOTSensorType.Companion.getSensorTypeByDescription(
                                text.replace(MCU_PREFIX, "")
                            )
                        )
                        switchSensorType()
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onChannelSwitchClick() {
            val selectedIndex = unUsedChannelList.indexOf(mStates.channel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", unUsedChannelList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.channel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            checkValueIsValidAndUpdateSensor()
        }
    }

    private fun checkValueIsValidAndUpdateSensor() {
        when (mStates.sensorType.get()) {
            IOTSensorType.KANG_PERCOLATE //基康渗压计(BGK-4500)
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入触发值")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的触发值!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入修正值")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入多项式系数A")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的多项式系数A!")
                    return
                }

                if (mStates.extension4Value.get().isEmpty()) {
                    showMessageDialog("请输入多项式系数B")
                    return
                }
                try {
                    val value = mStates.extension4Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的多项式系数B!")
                    return
                }

                if (mStates.extension5Value.get().isEmpty()) {
                    showMessageDialog("请输入多项式系数C")
                    return
                }
                try {
                    val value = mStates.extension5Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的多项式系数C!")
                    return
                }

                if (mStates.extension6Value.get().isEmpty()) {
                    showMessageDialog("请输入温度系数K")
                    return
                }
                try {
                    val value = mStates.extension6Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的温度系数K!")
                    return
                }

                if (mStates.extension7Value.get().isEmpty()) {
                    showMessageDialog("请输入初始温度T0")
                    return
                }
                try {
                    val value = mStates.extension7Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的初始温度T0!")
                    return
                }

                if (mStates.extension8Value.get().isEmpty()) {
                    showMessageDialog("请输入绳长")
                    return
                }
                try {
                    val value = mStates.extension8Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的绳长!")
                    return
                }

                if (mStates.extension9Value.get().isEmpty()) {
                    showMessageDialog("请输入安装高程")
                    return
                }
                try {
                    val value = mStates.extension9Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的安装高程!")
                    return
                }
            }

            IOTSensorType.GUDAN_PERCOLATE //葛南渗压计(VWP-03)
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入触发值")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的触发值!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入修正值")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入灵敏度K")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的灵敏度K!")
                    return
                }

                if (mStates.extension4Value.get().isEmpty()) {
                    showMessageDialog("请输入温修系数B")
                    return
                }
                try {
                    val value = mStates.extension4Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的温修系数B!")
                    return
                }

                if (mStates.extension5Value.get().isEmpty()) {
                    showMessageDialog("请输入基准值F0")
                    return
                }
                try {
                    val value = mStates.extension5Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的基准值F0!")
                    return
                }

                if (mStates.extension6Value.get().isEmpty()) {
                    showMessageDialog("请输入初始温度T0")
                    return
                }
                try {
                    val value = mStates.extension6Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的初始温度T0!")
                    return
                }

                if (mStates.extension7Value.get().isEmpty()) {
                    showMessageDialog("请输入绳长")
                    return
                }
                try {
                    val value = mStates.extension7Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的绳长!")
                    return
                }

                if (mStates.extension8Value.get().isEmpty()) {
                    showMessageDialog("请输入安装高程")
                    return
                }
                try {
                    val value = mStates.extension8Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的安装高程!")
                    return
                }
            }

            IOTSensorType.JUNXING_ZLJ_300T //轴力计(ZLJ-300T)
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入触发值")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的触发值!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入修正值")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入灵敏度K")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的灵敏度K!")
                    return
                }

                if (mStates.extension4Value.get().isEmpty()) {
                    showMessageDialog("请输入温修系数B")
                    return
                }
                try {
                    val value = mStates.extension4Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的温修系数B!")
                    return
                }

                if (mStates.extension5Value.get().isEmpty()) {
                    showMessageDialog("请输入基准值F0")
                    return
                }
                try {
                    val value = mStates.extension5Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的基准值F0!")
                    return
                }

                if (mStates.extension6Value.get().isEmpty()) {
                    showMessageDialog("请输入初始温度T0")
                    return
                }
                try {
                    val value = mStates.extension6Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的初始温度T0!")
                    return
                }
            }

            IOTSensorType.GUDAN_STRESS //应力计
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入触发值")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的触发值!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入修正值")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入灵敏度K")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的灵敏度K!")
                    return
                }

                if (mStates.extension4Value.get().isEmpty()) {
                    showMessageDialog("请输入温修系数B")
                    return
                }
                try {
                    val value = mStates.extension4Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的温修系数B!")
                    return
                }

                if (mStates.extension5Value.get().isEmpty()) {
                    showMessageDialog("请输入基准值F0")
                    return
                }
                try {
                    val value = mStates.extension5Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的基准值F0!")
                    return
                }

                if (mStates.extension6Value.get().isEmpty()) {
                    showMessageDialog("请输入初始温度T0")
                    return
                }
                try {
                    val value = mStates.extension6Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的初始温度T0!")
                    return
                }

                if (mStates.extension7Value.get().isEmpty()) {
                    showMessageDialog("请输入膨胀系数")
                    return
                }
                try {
                    val value = mStates.extension7Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的膨胀系数!")
                    return
                }
            }

            IOTSensorType.VIBRATING_SENSOR//MCU 振弦传感器
            -> {

                if (sensorInfo.sens_k != IOTConstants.NULL_KEY) {
                    if (mStates.extension1Value.get().isEmpty()) {
                        showMessageDialog("请输入灵敏度K")
                        return
                    }
                    try {
                        val value = mStates.extension1Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的灵敏度K!")
                        return
                    }

                    if (mStates.extension2Value.get().isEmpty()) {
                        showMessageDialog("请输入温修系数B")
                        return
                    }
                    try {
                        val value = mStates.extension2Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的温修系数B!")
                        return
                    }

                    if (mStates.extension3Value.get().isEmpty()) {
                        showMessageDialog("请输入基准值F0")
                        return
                    }
                    try {
                        val value = mStates.extension3Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的基准值F0!")
                        return
                    }

                    if (mStates.extension4Value.get().isEmpty()) {
                        showMessageDialog("请输入初始温度T0")
                        return
                    }
                    try {
                        val value = mStates.extension4Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的初始温度T0!")
                        return
                    }
                }
                if (mStates.isExtension5Support.get() && mStates.extension5Value.get().isEmpty()) {
                    showMessageDialog("请输入测站编码")
                    return
                }
            }

            IOTSensorType.WEIR//MCU 量水堰计
            -> {
                if (mStates.isExtension3Support.get() && mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入测站编码")
                    return
                }
            }

            IOTSensorType.WATER_LEVEL_GAUGE//MCU 水位(液位)计
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入修正值")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入绳长")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的绳长!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入安装高程")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的安装高程!")
                    return
                }
            }

            else -> {}
        }
        updateSensorInfo()
    }

    private fun updateSensorInfo() {
        val sensorInfo = DasExternalSensorInfo()
        sensorInfo.type = mStates.sensorType.get().code
        sensorInfo.addr = (mStates.channel.get().toInt() - 1).toString()
        when (mStates.sensorType.get()) {
            IOTSensorType.KANG_PERCOLATE //基康渗压计(BGK-4500)
            -> {
                sensorInfo.threshold = mStates.extension1Value.get()
                sensorInfo.corrval = mStates.extension2Value.get().ifEmpty { "0" }
                sensorInfo.poly_a = mStates.extension3Value.get()
                sensorInfo.poly_b = mStates.extension4Value.get()
                sensorInfo.poly_c = mStates.extension5Value.get()
                sensorInfo.temp_k = mStates.extension6Value.get()
                sensorInfo.temp_t0 = mStates.extension7Value.get().ifEmpty { "0" }
                sensorInfo.ropelen = mStates.extension8Value.get()
                sensorInfo.tubealti = mStates.extension9Value.get()
            }

            IOTSensorType.GUDAN_PERCOLATE //葛南渗压计(VWP-03)
            -> {
                sensorInfo.threshold = mStates.extension1Value.get()
                sensorInfo.corrval = mStates.extension2Value.get().ifEmpty { "0" }
                sensorInfo.sens_k = mStates.extension3Value.get()
                sensorInfo.temp_b = mStates.extension4Value.get()
                sensorInfo.referval_f = mStates.extension5Value.get()
                sensorInfo.temp_t0 = mStates.extension6Value.get().ifEmpty { "0" }
                sensorInfo.ropelen = mStates.extension7Value.get()
                sensorInfo.tubealti = mStates.extension8Value.get()
            }

            IOTSensorType.JUNXING_ZLJ_300T //轴力计(ZLJ-300T)
            -> {
                sensorInfo.threshold = mStates.extension1Value.get()
                sensorInfo.corrval = mStates.extension2Value.get().ifEmpty { "0" }
                sensorInfo.sens_k = mStates.extension3Value.get()
                sensorInfo.temp_b = mStates.extension4Value.get()
                sensorInfo.referval_f = mStates.extension5Value.get().ifEmpty { "0" }
                sensorInfo.temp_t0 = mStates.extension6Value.get().ifEmpty { "0" }
            }

            IOTSensorType.GUDAN_STRESS //应力计
            -> {
                sensorInfo.threshold = mStates.extension1Value.get()
                sensorInfo.corrval = mStates.extension2Value.get().ifEmpty { "0" }
                sensorInfo.sens_k = mStates.extension3Value.get()
                sensorInfo.temp_b = mStates.extension4Value.get()
                sensorInfo.referval_f = mStates.extension5Value.get().ifEmpty { "0" }
                sensorInfo.temp_t0 = mStates.extension6Value.get().ifEmpty { "0" }
                sensorInfo.elastic_mod = mStates.extension7Value.get()
            }

            IOTSensorType.VIBRATING_SENSOR//MCU 振弦传感器
            -> {
                sensorInfo.threshold = IOTConstants.NULL_KEY
                sensorInfo.corrval = IOTConstants.NULL_KEY
                if (sensorInfo.sens_k != IOTConstants.NULL_KEY) {
                    sensorInfo.sens_k = mStates.extension1Value.get()
                    sensorInfo.temp_b = mStates.extension2Value.get()
                    sensorInfo.referval_f = mStates.extension3Value.get()
                    sensorInfo.temp_t0 = mStates.extension4Value.get()
                } else {
                    sensorInfo.sens_k = IOTConstants.NULL_KEY
                    sensorInfo.temp_b = IOTConstants.NULL_KEY
                    sensorInfo.referval_f = IOTConstants.NULL_KEY
                    sensorInfo.temp_t0 = IOTConstants.NULL_KEY
                }
                sensorInfo.caddr = mStates.extension5Value.get()
            }

            IOTSensorType.WEIR//MCU 量水堰计
            -> {
                sensorInfo.threshold = IOTConstants.NULL_KEY
                sensorInfo.corrval = IOTConstants.NULL_KEY
                sensorInfo.lsycsds = IOTConstants.NULL_KEY
                sensorInfo.lsyysst = IOTConstants.NULL_KEY
                sensorInfo.caddr = mStates.extension3Value.get()
            }

            IOTSensorType.WATER_LEVEL_GAUGE//MCU 水位(液位)计
            -> {
                sensorInfo.threshold = IOTConstants.NULL_KEY
                sensorInfo.corrval = mStates.extension1Value.get().ifEmpty { "0" }
                sensorInfo.ropelen = mStates.extension2Value.get()
                sensorInfo.tubealti = mStates.extension3Value.get()
            }

            IOTSensorType.GENERAL_STRING_INSTRUMENT //通用弦式仪
                -> {
                sensorInfo.threshold = mStates.extension1Value.get()
            }

            else -> {}
        }

        //更新或者添加传感器
        if (sensorListViewModel.sensorModelMap.containsKey(sensorChannel)) {
            sensorListViewModel.sensorModelMap.remove(sensorChannel)
        }
        sensorListViewModel.sensorModelMap[sensorInfo.addr] = sensorInfo
        sensorListViewModel.updateIsRefreshSensorList(true)

        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val MCU_PREFIX = "MCU_"

        fun newBundleArguments(
            sensorChannel: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(AppContants.Extras.SENSOR_CHANNEL, sensorChannel)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}