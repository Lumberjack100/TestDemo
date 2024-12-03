package com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.commonlib.utils.AppContants.Extras.Companion.SENSOR_ADDR
import com.shmedo.core.commonlib.utils.AppContants.Extras.Companion.SENSOR_INDEX
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasExternalSensorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseDasExternalDigitalSensorClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasExternalDigitalSensorBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalDigitalSensorViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/2/5
 * @desc: 数字式传感器配置
 *
 */
@Deprecated("This class is deprecated", ReplaceWith("DasExternalDigitalSensorFragment"))
class OldDasExternalDigitalSensorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasExternalDigitalSensorBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasExternalDigitalSensorViewModel
    private lateinit var sensorListViewModel: DasExternalSensorListViewModel<DasExternalSensorInfo>

    private val iotSensorType: IOTSensorType by lazy {
        IOTSensorType.getSensorTypeByCollectorCode(sensorListViewModel.collectorType.get())
    }
    private val usedAddressList = ArrayList<String>()
    private var sensorIndex: Int = -1
    private var sensorAddr = "-1"

    //阵列测斜仪物模型
    private val modelTypeList = arrayOf("坐标模型", "ADME 模型")

    //子雷达类型
    private val childRadarTypeList = arrayOf("雷达物位计", "精波雷达")
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        sensorListViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_external_digital_sensor,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasExternalDigitalSensorBinding
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
        binding.llToolbar.toolbar.title = iotSensorType.description
        arguments?.let {
            sensorIndex = it.getInt(SENSOR_INDEX, -1)
            sensorAddr = it.getString(SENSOR_ADDR, "-1")
        }
        usedAddressList.clear()
        sensorListViewModel.sensorModelMap.keys.filterNot { it == sensorAddr }
            .forEach { usedAddressList.add(it) }

        val externalSensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorAddr))
            sensorListViewModel.sensorModelMap[sensorAddr]!!
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
        mStates.address.set(sensorInfo.addr)
        sensorInfo.threshold.toDoubleOrNull()?.let {
            mStates.triggerValue.set(decimalFormat.format(it))
        }
        sensorInfo.corrval.toDoubleOrNull()?.let {
            mStates.correctValue.set(decimalFormat.format(it))
        }
        try {
            when (iotSensorType) {
                IOTSensorType.RAIN_GAUGE,//压电式雨量计
                IOTSensorType.WIRE_SHIFT //拉线位移计
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("修正值（米）")
                }

                IOTSensorType.SOIL_MOISTURE //土壤含水率
                -> {
                    mStates.triggerTitle.set("触发值(%rh)")
                    mStates.correctTitle.set("修正值(%rh)")
                }

                IOTSensorType.INCLINOMETER //测斜仪
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("修正值（米）")
                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("测段长（毫米）")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.spacing.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }
                    if (sensorInfo.model_type != IOTConstants.NULL_KEY) {
                        mStates.isModelSwitchSupport.set(true)
                        sensorInfo.model_type.toIntOrNull()?.let {
                            if (it < modelTypeList.size) {
                                mStates.modelType.set(modelTypeList[it])
                            }
                        }
                        mStates.isExtension2Support.set(true)
                        mStates.extension2Title.set("解算方式")
                        mStates.extension2Value.set(if (sensorInfo.datatype == "0") "顶部" else "底部")
                        mStates.extension2ValueEnable.set(false)

                        mStates.isExtension3Support.set(true)
                        mStates.extension3Title.set("测量间隔（毫秒）")
                        mStates.extension3Value.set(sensorInfo.measinval)
                        mStates.extension3ValueEnable.set(false)
                    }
                }

                IOTSensorType.ULTRASONIC_LEVEL_GAUGE //超声波物位计
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("安装高程（米）")
                }

                IOTSensorType.RADAR_LEVEL_GAUGE //雷达物位计
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("安装高程（米）")
                    if (sensorInfo.child_type != IOTConstants.NULL_KEY) {
                        mStates.isChildSensorTypeSupport.set(true)
                        mStates.childSensorTypeTitle.set("雷达类型")
                        sensorInfo.child_type.toIntOrNull()?.let {
                            if (it < childRadarTypeList.size) {
                                mStates.childSensorType.set(childRadarTypeList[it])
                            }
                        }
                    }
                }

                IOTSensorType.LUYAN_INCLINOMETER //倾角仪
                -> {
                    mStates.triggerTitle.set("触发值(°)")
                    mStates.isCorrectSupport.set(false)

                    mStates.isExtension1Support.set(true)
                    mStates.isExtension1TipBtnSupport.set(true)
                    mStates.extension1Title.set("X轴初始角度(°)")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.initvalx.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.isExtension2TipBtnSupport.set(true)
                    mStates.extension2Title.set("Y轴初始角度(°)")
                    sensorInfo.initvaly.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension3Support.set(true)
                    mStates.isExtension3TipBtnSupport.set(true)
                    mStates.extension3Title.set("Z轴初始角度(°)")
                    sensorInfo.initvalz.toDoubleOrNull()?.let {
                        mStates.extension3Value.set(decimalFormat.format(it))
                    }
                }

                IOTSensorType.INFRASOUND //次声
                -> {
                    mStates.triggerTitle.set("触发值(Hz)")
                    mStates.correctTitle.set("修正值(Hz)")
                }

                IOTSensorType.WEIR //量水堰计
                -> {
                    mStates.triggerTitle.set("触发值(m³/s)")
                    mStates.correctTitle.set("修正值（毫米）")
                    mStates.isCorrectTipBtnSupport.set(true)

                    mStates.isExtension1Support.set(sensorInfo.lsycsds != IOTConstants.NULL_KEY)
                    mStates.isExtension1TipBtnSupport.set(sensorInfo.lsycsds != IOTConstants.NULL_KEY)
                    mStates.extension1Title.set("初始读数（毫米）")
                    if (mStates.isExtension1Support.get()) {
                        decimalFormat.applyPattern("#.#")
                        sensorInfo.lsycsds.toDoubleOrNull()?.let {
                            mStates.extension1Value.set(decimalFormat.format(it))
                        }
                    } else {
                        mStates.extension1Value.set(IOTConstants.NULL_KEY)
                    }

                    mStates.isExtension2Support.set(sensorInfo.lsyysst != IOTConstants.NULL_KEY)
                    mStates.isExtension2TipBtnSupport.set(sensorInfo.lsyysst != IOTConstants.NULL_KEY)
                    mStates.extension2Title.set("初始堰上水头（毫米）")
                    if (mStates.isExtension2Support.get()) {
                        decimalFormat.applyPattern("#.#")
                        sensorInfo.lsyysst.toDoubleOrNull()?.let {
                            mStates.extension2Value.set(decimalFormat.format(it))
                        }
                    } else {
                        mStates.extension2Value.set(IOTConstants.NULL_KEY)
                    }

                    mStates.isExtension3Support.set(sensorInfo.caddr != IOTConstants.NULL_KEY)
                    mStates.isExtension3TipBtnSupport.set(false)
                    mStates.extension3Title.set("测站编码")
                    mStates.extension3Value.set(if (mStates.isExtension3Support.get()) sensorInfo.caddr else IOTConstants.NULL_KEY)
                }

                IOTSensorType.STATIC_LEVEL,//静力水准
                IOTSensorType.SEDIMENTATION_METER ,//沉降仪
                IOTSensorType.VERTICAL_COORDINATE ,//垂线坐标仪
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("修正值（毫米）")

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("初始值（毫米）")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.initval.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension1ButtonSupport.set(sensorIndex != -1)
                }

                IOTSensorType.WEATHER_STATION, //气象计
                IOTSensorType.TURBIDITY_METER //浊度仪
                -> {
                    mStates.triggerTitle.set("触发值（米/秒）")
                    mStates.correctTitle.set("修正值（米/秒）")
                }

                IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
                -> {
                    mStates.triggerTitle.set("触发值（毫米）")
                    mStates.correctTitle.set("修正值（毫米）")

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("安装高程（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.tubealti.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("绳长（米）")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.ropelen.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }
                }

                else -> {

                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    inner class ClickProxy : BaseDasExternalDigitalSensorClickProxy() {
        /**
         * 阵列测斜仪选择物模型
         */
        override fun onModelSwitchClick() {
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

        /**
         * 选择子雷达传感器类型
         */
        override fun onChildSensorTypeSwitchClick() {
            val selectedIndex = childRadarTypeList.indexOf(mStates.childSensorType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "                                                                                                                                                                                                                            ",
                    childRadarTypeList,
                    null,
                    selectedIndex,
                    { position, text ->
                        mStates.childSensorType.set(text)
                    },
                    0,
                    R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onTriggerTipBtnClick() {

        }

        /**
         * 修正值提示按钮
         */
        override fun onCorrectTipBtnClick() {
            if (iotSensorType == IOTSensorType.WEIR) //量水堰计修正值
                showMessageDialog("修正浮子高度  ")
        }

        /**
         * 扩展1提示按钮
         */
        override fun onExtension1TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算!")
            else if (iotSensorType == IOTSensorType.WEIR) //量水堰计初始读数
                showMessageDialog("当初始读数设置值小于 0  时，设备将自动计算初始值!")
        }

        /**
         * 扩展2提示按钮
         */
        override fun onExtension2TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算")
            else if (iotSensorType == IOTSensorType.WEIR) //量水堰计初始堰上水头
                showMessageDialog("当水经堰顶点流出时，设置值为堰顶点到水面的距离；否则，设置值为堰顶点到浮子距离的负值；")
        }

        /**
         * 扩展3提示按钮
         */
        override fun onExtension3TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算!")
        }

        /**
         * 扩展4提示按钮
         */
        override fun onExtension4TipBtnClick() {

        }

        /**
         * 扩展5提示按钮
         */
        override fun onExtension5TipBtnClick() {

        }

        override fun onExtension1ButtonClick() {
            if (iotSensorType == IOTSensorType.STATIC_LEVEL
                || iotSensorType == IOTSensorType.SEDIMENTATION_METER
                || iotSensorType == IOTSensorType.VERTICAL_COORDINATE) { //静力水准/沉降仪/垂线坐标仪 初始值重置
                if (TextUtils.isEmpty(mStates.address.get())) {
                    Toaster.show("传感器地址不能为空!")
                    return
                }
                resetInitValue()
            }
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            checkValueIsValidAndUpdateSensor()
        }
    }

    /**
     * 重置静力水准初始值
     */
    private fun resetInitValue() {
        commandItems.clear()

        val entity = DasExternalSensorEntity().apply {
            index = sensorIndex.toString()
            type = iotSensorType.code
            addr = mStates.address.get()
            threshold = mStates.triggerValue.get()
            corrval = mStates.correctValue.get()
            initval = "FFFFFFFF"
        }
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_10000_MILLIS
        )
    }

    private fun checkValueIsValidAndUpdateSensor() {
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入传感器地址!")
            return
        }
        try {
            val value = mStates.address.get().toInt()
            if (value < 0) {
                showMessageDialog("请输入正确的传感器地址!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的传感器地址!")
            return
        }
        //传感器地址不能重复,进行检查
        if (usedAddressList.contains(mStates.address.get())) {
            showMessageDialog("传感器地址已被占用!")
            return
        }

        if (mStates.isTriggerSupport.get()) {
            if (mStates.triggerValue.get().isEmpty()) {
                showMessageDialog("请输入触发值!")
                return
            }
            try {
                val value = mStates.triggerValue.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的触发值!")
                return
            }
        }

        if (mStates.isCorrectSupport.get()) {
            if (mStates.correctValue.get().isEmpty()) {
                showMessageDialog("请输入修正值!")
                return
            }
            try {
                val value = mStates.correctValue.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的修正值!")
                return
            }
        }
        when (iotSensorType) {
            IOTSensorType.INCLINOMETER //测斜仪
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入测段长!")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的测段长!")
                    return
                }
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入X轴初始角度!")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的X轴初始角度!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入Y轴初始角度!")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的Y轴初始角度!")
                    return
                }

                if (mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入Z轴初始角度!")
                    return
                }
                try {
                    val value = mStates.extension3Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的Z轴初始角度!")
                    return
                }
            }

            IOTSensorType.WEIR //量水堰计
            -> {
                if (mStates.isExtension1Support.get()){
                    if (mStates.extension1Value.get().isEmpty()) {
                        showMessageDialog("请输入初始读数!")
                        return
                    }
                    try {
                        val value = mStates.extension1Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的初始读数!")
                        return
                    }
                }
                if (mStates.isExtension2Support.get()){
                    if (mStates.extension2Value.get().isEmpty()) {
                        showMessageDialog("请输入初始堰上水头!")
                        return
                    }
                    try {
                        val value = mStates.extension2Value.get().toDouble()
                    } catch (ex: Exception) {
                        showMessageDialog("请输入正确的初始堰上水头!")
                        return
                    }
                }
                if (mStates.isExtension3Support.get() && mStates.extension3Value.get().isEmpty()) {
                    showMessageDialog("请输入测站编码!")
                    return
                }
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER, //沉降仪
            IOTSensorType.VERTICAL_COORDINATE ,//垂线坐标仪
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入初始值!")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的初始值!")
                    return
                }
            }

            IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
            -> {
                if (mStates.extension1Value.get().isEmpty()) {
                    showMessageDialog("请输入安装高程!")
                    return
                }
                try {
                    val value = mStates.extension1Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的安装高程!")
                    return
                }

                if (mStates.extension2Value.get().isEmpty()) {
                    showMessageDialog("请输入绳长!")
                    return
                }
                try {
                    val value = mStates.extension2Value.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的绳长!")
                    return
                }
            }

            else -> {}
        }
        updateSensorInfo()
    }

    private fun updateSensorInfo() {
        val sensorInfo = DasExternalSensorInfo()
        sensorInfo.type = iotSensorType.code
        sensorInfo.addr = mStates.address.get()
        sensorInfo.threshold = mStates.triggerValue.get()
        sensorInfo.corrval = mStates.correctValue.get()

        when (iotSensorType) {
            IOTSensorType.INCLINOMETER //测斜仪
            -> {
                sensorInfo.spacing = mStates.extension1Value.get()
                if (mStates.isModelSwitchSupport.get())
                    sensorInfo.model_type =
                        modelTypeList.indexOf(mStates.modelType.get()).toString()
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                sensorInfo.initvalx = mStates.extension1Value.get()
                sensorInfo.initvaly = mStates.extension2Value.get()
                sensorInfo.initvalz = mStates.extension3Value.get()
            }

            IOTSensorType.WEIR //量水堰计
            -> {
                sensorInfo.lsycsds = mStates.extension1Value.get()
                sensorInfo.lsyysst = mStates.extension2Value.get()
                sensorInfo.caddr = mStates.extension3Value.get()
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER ,//沉降仪
            IOTSensorType.VERTICAL_COORDINATE ,//垂线坐标仪
            -> {
                sensorInfo.initval = mStates.extension1Value.get()
            }

            IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
            -> {
                sensorInfo.tubealti = mStates.extension1Value.get()
                sensorInfo.ropelen = mStates.extension2Value.get()
            }

            IOTSensorType.RADAR_LEVEL_GAUGE //雷达液(物)位计 设置子雷达传感器型号
            -> {
                if (mStates.isChildSensorTypeSupport.get())
                    sensorInfo.child_type =
                        childRadarTypeList.indexOf(mStates.childSensorType.get()).toString()
            }

            else -> {}
        }

        //更新或者添加传感器
        if (sensorListViewModel.sensorModelMap.containsKey(sensorAddr)) {
            sensorListViewModel.sensorModelMap.remove(sensorAddr)
        }
        sensorListViewModel.sensorModelMap[sensorInfo.addr] = sensorInfo
        sensorListViewModel.updateIsRefreshSensorList(true)

        nav().navigateUp()
    }

    override fun setResultData(cmdStr: String) {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {

        fun newBundleArguments(
            index: Int,
            sensorAddr: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(AppContants.Extras.SENSOR_INDEX, index)
            putString(com.shmedo.core.commonlib.utils.AppContants.Extras.SENSOR_ADDR, sensorAddr)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}