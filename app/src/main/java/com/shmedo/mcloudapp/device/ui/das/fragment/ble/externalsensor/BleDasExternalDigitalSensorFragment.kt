package com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.core.commonlib.utils.AppContants.Extras.Companion.SENSOR_ADDR
import com.shmedo.core.commonlib.utils.AppContants.Extras.Companion.SENSOR_INDEX
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasExternalDigitalSensorBinding
import com.shmedo.mcloudapp.device.common.BaseDasExternalDigitalSensorClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalDigitalSensorViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import org.koin.android.ext.android.inject
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
@Deprecated("This class is deprecated", ReplaceWith("NewBleDasExternalDigitalSensorFragment"))
class BleDasExternalDigitalSensorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasExternalDigitalSensorBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasExternalDigitalSensorViewModel
    private lateinit var sensorListViewModel: DasExternalSensorListViewModel<DasExternalSensorInfo>
    val mdParseManager: MDParserManager by inject()

    private val iotSensorType: IOTSensorType by lazy {
        IOTSensorType.getSensorTypeByCollectorCode(sensorListViewModel.collectorType.get())
    }
    private val usedAddressList = ArrayList<String>()
    private var sensorIndex: Int = -1
    private var sensorAddr = "-1"

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

        val sensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorAddr))
            sensorListViewModel.sensorModelMap[sensorAddr]!!
        else {
            DasExternalSensorInfo(
                addr = "",
                type = iotSensorType.code,
                threshold = "",
                corrval = ""
            )
        }
        initSensorInfo(sensorInfo)
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
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.correctTitle.set("修正值(单位:米)")
                }

                IOTSensorType.SOIL_MOISTURE //土壤含水率
                -> {
                    mStates.triggerTitle.set("触发值(单位:%rh)")
                    mStates.correctTitle.set("修正值(单位:%rh)")
                }

                IOTSensorType.INCLINOMETER //测斜仪
                -> {
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.correctTitle.set("修正值(单位:米)")
                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("测段长(单位:毫米)")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.spacing.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }
                }

                IOTSensorType.ULTRASONIC_LEVEL_GAUGE, //超声波物位计
                IOTSensorType.RADAR_LEVEL_GAUGE -> {//雷达物位计
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.correctTitle.set("安装高程(单位:米)")
                }

                IOTSensorType.LUYAN_INCLINOMETER //倾角仪
                -> {
                    mStates.triggerTitle.set("触发值(单位:°)")
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
                    mStates.triggerTitle.set("触发值(单位:Hz)")
                    mStates.correctTitle.set("修正值(单位:Hz)")
                }

                IOTSensorType.WEIR //量水堰计
                -> {
                    mStates.triggerTitle.set("触发值(单位:m³/s)")
                    mStates.correctTitle.set("修正值(单位:毫米)")
                    mStates.isCorrectTipBtnSupport.set(true)

                    mStates.isExtension1Support.set(true)
                    mStates.isExtension1TipBtnSupport.set(true)
                    mStates.extension1Title.set("初始读数(毫米)")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.lsycsds.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.isExtension2TipBtnSupport.set(true)
                    mStates.extension2Title.set("初始堰上水头(毫米)")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.lsyysst.toDoubleOrNull()?.let {
                        mStates.extension2Value.set(decimalFormat.format(it))
                    }
                }

                IOTSensorType.STATIC_LEVEL,//静力水准
                IOTSensorType.SEDIMENTATION_METER,//沉降仪
                -> {
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.correctTitle.set("修正值(单位:毫米)")

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("初始值(毫米)")
                    decimalFormat.applyPattern("#.#")
                    sensorInfo.initval.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension1ButtonSupport.set(sensorIndex != -1)
                }

                IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                -> {
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.isCorrectSupport.set(false)

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("X轴初始值(毫米)")
                    mStates.extension1Value.set(
                        DeviceStatusInfoProcessor.formatDoubleValue(
                            sensorInfo.corrval,
                            "0",
                            3
                        )
                    )

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("Y轴初始值(毫米)")
                    mStates.extension2Value.set(
                        DeviceStatusInfoProcessor.formatDoubleValue(
                            sensorInfo.initval,
                            "0",
                            3
                        )
                    )

                    mStates.isExtension1ButtonSupport.set(sensorIndex != -1)
                }

                IOTSensorType.WEATHER_STATION, //气象计
                IOTSensorType.TURBIDITY_METER //浊度仪
                -> {
                    mStates.triggerTitle.set("触发值(单位:米/秒)")
                    mStates.correctTitle.set("修正值(单位:米/秒)")
                }

                IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
                -> {
                    mStates.triggerTitle.set("触发值(单位:毫米)")
                    mStates.correctTitle.set("修正值(单位:毫米)")

                    mStates.isExtension1Support.set(true)
                    mStates.extension1Title.set("安装高程(米)")
                    decimalFormat.applyPattern("#.###")
                    sensorInfo.tubealti.toDoubleOrNull()?.let {
                        mStates.extension1Value.set(decimalFormat.format(it))
                    }

                    mStates.isExtension2Support.set(true)
                    mStates.extension2Title.set("绳长(米)")
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
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    inner class ClickProxy : BaseDasExternalDigitalSensorClickProxy() {
        /**
         * 阵列测斜仪选择物模型
         */
        override fun onModelSwitchClick() {

        }

        /**
         * 选择子雷达传感器类型
         */
        override fun onChildSensorTypeSwitchClick() {

        }

        /**
         * 触发阈值提示弹窗
         */
        override fun onTriggerTipBtnClick() {

        }

        /**
         * 修正值提示弹窗
         */
        override fun onCorrectTipBtnClick() {
            if (iotSensorType == IOTSensorType.WEIR) //量水堰计修正值
                showMessageDialog("修正浮子高度  ")
        }

        /**
         * 扩展1提示弹窗
         */
        override fun onExtension1TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算!")
            else if (iotSensorType == IOTSensorType.WEIR) //量水堰计初始读数
                showMessageDialog("当初始读数设置值小于 0  时，设备将自动计算初始值!")
        }

        /**
         * 扩展2提示弹窗
         */
        override fun onExtension2TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算")
            else if (iotSensorType == IOTSensorType.WEIR) //量水堰计初始堰上水头
                showMessageDialog("当水经堰顶点流出时，设置值为堰顶点到水面的距离；否则，设置值为堰顶点到浮子距离的负值；")
        }

        /**
         * 扩展3提示弹窗
         */
        override fun onExtension3TipBtnClick() {
            if (iotSensorType == IOTSensorType.LUYAN_INCLINOMETER) //倾角仪
                showMessageDialog("初始值大于 360，设备将自动计算!")
        }

        /**
         * 扩展4提示弹窗
         */
        override fun onExtension4TipBtnClick() {

        }

        /**
         * 扩展5提示弹窗
         */
        override fun onExtension5TipBtnClick() {

        }

        /**
         * 扩展1 按钮
         */
        override fun onExtension1ButtonClick() {
            if (iotSensorType == IOTSensorType.STATIC_LEVEL
                || iotSensorType == IOTSensorType.SEDIMENTATION_METER
                || iotSensorType == IOTSensorType.VERTICAL_COORDINATE
            ) { //静力水准/沉降仪/垂线坐标仪初始值重置
                if (TextUtils.isEmpty(mStates.address.get())) {
                    Toaster.show("传感器地址不能为空!")
                    return
                }
                resetInitValue()
            }
        }

        /**
         * 确定按钮
         */
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
        val command =
            MDCommandUtil.getCommand(
                MDCommandType.SENSOR_INITIAL_READING,
                "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                    MDCommandUtil.formatStringTwo(mStates.address.get())
                }FFFFFFFF"
            )
        commandItems.add(command)

        refreshData()

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = com.shmedo.core.commonlib.utils.AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 重置静力水准初始值后 刷新数据
     */
    private fun refreshData() {
        //查询静力水准配置信息
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
            "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                MDCommandUtil.formatStringTwo(sensorIndex.toString())
            }"
        )
        commandItems.add(command)
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

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER,//沉降仪
            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
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
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER,//沉降仪
            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
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
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.SENSOR_INITIAL_READING -> {//设置量水堰初始读数 171
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "量水堰初始读数设置出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER -> {//获取XX采集器YY通道的传感器参数 ##101
                val result = mdParseManager.parse<DasExternalSensorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "刷新传感器参数出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initSensorInfo(result.data)
                        sendCommandFromCmdList()
                    }
                }
            }

            else -> {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}