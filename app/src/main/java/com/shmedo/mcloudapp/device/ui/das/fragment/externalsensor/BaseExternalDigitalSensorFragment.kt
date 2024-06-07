package com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor

import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasExternalSensorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentBaseExternalDigitalSensorBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.ExternalDigitalSensorParamButtonItem
import com.shmedo.mcloudapp.device.model.ExternalDigitalSensorParamChooseItem
import com.shmedo.mcloudapp.device.model.ExternalDigitalSensorParamEditItem
import com.shmedo.mcloudapp.device.model.GapItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.formatDoubleValue
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import timber.log.Timber

open class BaseExternalDigitalSensorFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentBaseExternalDigitalSensorBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
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


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        sensorListViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_base_external_digital_sensor,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBaseExternalDigitalSensorBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initAdapter()
    }

    override fun initData() {
        super.initData()
        binding.llToolbar.toolbar.title = iotSensorType.description
        arguments?.let {
            sensorIndex = it.getInt(AppContants.Extras.SENSOR_INDEX, -1)
            sensorAddr = it.getString(AppContants.Extras.SENSOR_ADDR, "-1")
        }
        usedAddressList.clear()
        sensorListViewModel.sensorModelMap.keys.filterNot { it == sensorAddr }
            .forEach { usedAddressList.add(it) }

        val externalSensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorAddr))
            sensorListViewModel.sensorModelMap[sensorAddr]!!
        else {
            DasExternalSensorInfo(
                addr = "-1",
                type = iotSensorType.code,
                threshold = "",
                corrval = ""
            )
        }
        initSensorInfo(externalSensorInfo)
    }

    private fun initSensorInfo(sensorInfo: DasExternalSensorInfo) {
        val groupList = mutableListOf<Any>()
        try {
            groupList.add(
                ExternalDigitalSensorParamEditItem(
                    name = "传感器地址",
                    value = sensorInfo.addr,
                    inputTypeFilter = "number",
                    inputLengthFilter = 3,
                )
            )
            when (iotSensorType) {
                IOTSensorType.RAIN_GAUGE,//压电式雨量计
                IOTSensorType.WIRE_SHIFT //拉线位移计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                }

                IOTSensorType.SOIL_MOISTURE //土壤含水率
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:%rh)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:%rh)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                }

                IOTSensorType.INCLINOMETER //测斜仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 1)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "测段长(单位:毫米)",
                            value = sensorInfo.spacing.formatDoubleValue("", 1)
                        )
                    )
                    sensorInfo.model_type.notNullKey { type ->
                        type.toIntOrNull()?.let { typeIndex ->
                            groupList.add(
                                0,
                                ExternalDigitalSensorParamChooseItem(
                                    name = "模型切换",
                                    value = if (typeIndex in modelTypeList.indices) modelTypeList[typeIndex] else modelTypeList[0]
                                )
                            )
                        }
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "解算方式",
                                value = if (sensorInfo.datatype == "0") "顶部" else "底部",
                                inputEnable = false
                            )
                        )
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "测量间隔(毫秒)",
                                value = sensorInfo.measinval.formatDoubleValue("", 1),
                                inputEnable = false
                            )
                        )
                    }
                }

                IOTSensorType.ULTRASONIC_LEVEL_GAUGE //超声波物位计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 1)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "安装高程(单位:米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                }

                IOTSensorType.RADAR_LEVEL_GAUGE //雷达物位计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 1)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "安装高程(单位:米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                    sensorInfo.child_type.notNullKey { type ->
                        type.toIntOrNull()?.let { typeIndex ->
                            groupList.add(
                                0,
                                ExternalDigitalSensorParamChooseItem(
                                    name = "雷达类型",
                                    value = if (typeIndex in childRadarTypeList.indices) childRadarTypeList[typeIndex] else childRadarTypeList[0]
                                )
                            )
                        }
                    }
                }

                IOTSensorType.LUYAN_INCLINOMETER //倾角仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:°)",
                            value = sensorInfo.threshold.formatDoubleValue("", 2)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "X轴初始角度(单位:°)",
                            value = sensorInfo.initvalx.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Y轴初始角度(单位:°)",
                            value = sensorInfo.initvaly.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Z轴初始角度(单位:°)",
                            value = sensorInfo.initvalz.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                }

                IOTSensorType.INFRASOUND //次声
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:Hz)",
                            value = sensorInfo.threshold.formatDoubleValue("", 2)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:Hz)",
                            value = sensorInfo.corrval.formatDoubleValue("", 2)
                        )
                    )
                }

                IOTSensorType.WEIR //量水堰计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:m³/s)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:毫米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 1),
                            desc = "修正浮子高度"
                        )
                    )
                    sensorInfo.lsycsds.notNullKey {
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "初始读数(毫米)",
                                value = it.formatDoubleValue("", 1),
                                desc = "当初始读数设置值小于 0  时，设备将自动计算初始值!"
                            )
                        )
                    }
                    sensorInfo.lsyysst.notNullKey {
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "初始堰上水头(毫米)",
                                value = it.formatDoubleValue("", 1),
                                desc = "当水经堰顶点流出时，设置值为堰顶点到水面的距离；否则，设置值为堰顶点到浮子距离的负值"
                            )
                        )
                    }
                    sensorInfo.caddr.notNullKey {
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "测站编码",
                                value = it.formatDoubleValue("", 0),
                            )
                        )
                    }
                }

                IOTSensorType.STATIC_LEVEL,//静力水准
                IOTSensorType.SEDIMENTATION_METER,//沉降仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "初始值(单位:毫米)",
                            value = sensorInfo.initval.formatDoubleValue("", 3),
                            btnVisible = sensorIndex != -1 //只有在编辑传感器下才显示重置按钮，新建传感器不显示
                        )
                    )
                }

                IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "X轴初始值(单位:毫米)",
                            value = sensorInfo.initvalx.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Y轴初始值(单位:毫米)",
                            value = sensorInfo.initvaly.formatDoubleValue("", 3),
                        )
                    )
                    if (sensorIndex != -1) {
                        groupList.add(
                            GapItem(
                                height = ConvertUtils.dp2px(30f)
                            )
                        )
                        groupList.add(
                            ExternalDigitalSensorParamButtonItem(
                                btnText = "重置初始值",
                            )
                        )
                    }
                }

                IOTSensorType.WEATHER_STATION, //气象计
                IOTSensorType.TURBIDITY_METER //浊度仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:米/秒)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:米/秒)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3),
                        )
                    )
                }

                IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(单位:毫米)",
                            value = sensorInfo.threshold.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(单位:毫米)",
                            value = sensorInfo.corrval.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "安装高程(单位:米)",
                            value = sensorInfo.tubealti.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "绳长(单位:米)",
                            value = sensorInfo.ropelen.formatDoubleValue("", 3)
                        )
                    )
                }

                else -> {

                }
            }

            groupList.add(
                GapItem(
                    height = ConvertUtils.dp2px(60f)
                )
            )
            groupList.add(
                ExternalDigitalSensorParamButtonItem(
                    btnText = "确定",
                )
            )
            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            addType<ExternalDigitalSensorParamChooseItem>(R.layout.item_das_external_digital_sensor_param_choose)
            addType<ExternalDigitalSensorParamEditItem>(R.layout.item_das_external_digital_sensor_param_edit)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            addType<ExternalDigitalSensorParamButtonItem>(R.layout.item_das_external_digital_sensor_param_summit_button)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_das_external_digital_sensor_param_choose -> {
                        val item = getModel<ExternalDigitalSensorParamChooseItem>()
                        when (item.name) {
                            "模型切换" -> {
                                onModelSwitchClick(item)
                            }

                            "雷达类型" -> {
                                onChildSensorTypeSwitchClick(item)
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
            R.id.btn_desc.onClick {
                val item = getModel<ExternalDigitalSensorParamEditItem>()
                showMessageDialog(item.desc)
            }
            R.id.btn_extension.onClick {
                if (iotSensorType == IOTSensorType.STATIC_LEVEL
                    || iotSensorType == IOTSensorType.SEDIMENTATION_METER
                    || iotSensorType == IOTSensorType.VERTICAL_COORDINATE
                ) { //静力水准/沉降仪/垂线坐标仪 初始值重置
                    if (sensorAddr != "-1") {
                        Toaster.show("传感器地址不能为空!")
                        return@onClick
                    }
                    resetInitValue()
                }
            }
            R.id.btn_submit.onClick {
                KeyboardUtils.hideSoftInput(binding.root)
                checkValueIsValidAndUpdateSensor()
            }
        }
    }

    /**
     * 阵列测斜仪选择物模型
     */
    private fun onModelSwitchClick(item: ExternalDigitalSensorParamChooseItem) {
        val selectedIndex = modelTypeList.indexOf(item.value)
        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asBottomList(
                "", modelTypeList,
                null, selectedIndex,
                { position, text ->
                    item.refreshValue(text)
                }, 0, R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    /**
     * 选择子雷达传感器类型
     */
    private fun onChildSensorTypeSwitchClick(item: ExternalDigitalSensorParamChooseItem) {
        val selectedIndex = childRadarTypeList.indexOf(item.value)
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
                    item.refreshValue(text)
                },
                0,
                R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    /**
     * 重置 静力水准/沉降仪/垂线坐标仪 初始值
     */
    private fun resetInitValue() {
        commandItems.clear()

        val address =
            binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                ?.findLast { it.name.contains("传感器地址") }?.value?.trim() ?: ""

        val entity = DasExternalSensorEntity().apply {
            index = sensorIndex.toString()
            type = iotSensorType.code
            addr = address
            initval =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) IOTConstants.NULL_KEY else "FFFFFFFF"
            initvalx =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) "0" else IOTConstants.NULL_KEY
            initvaly =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) "0" else IOTConstants.NULL_KEY
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
        val sensorInfo = DasExternalSensorInfo()
        sensorInfo.type = iotSensorType.code

        binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
            ?.findLast { it.name.contains("传感器地址") }?.let { item ->
                if (item.value.isEmpty()) {
                    showMessageDialog("请输入传感器地址!")
                    return
                }
                try {
                    val value = item.value.toInt()
                    if (value < 0) {
                        showMessageDialog("请输入正确的传感器地址!")
                        return
                    }
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的传感器地址!")
                    return
                }
                //传感器地址不能重复,进行检查
                if (usedAddressList.contains(item.value)) {
                    showMessageDialog("传感器地址已被占用!")
                    return
                }
                sensorInfo.addr = item.value
            }
        //触发值
        binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
            ?.findLast { it.name.contains("触发值") }?.let { item ->
                if (item.value.isEmpty()) {
                    showMessageDialog("请输入触发值!")
                    return
                }
                try {
                    val value = item.value.toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的触发值!")
                    return
                }
                sensorInfo.threshold = item.value
            }
        //修正值
        binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
            ?.findLast { it.name.contains("修正值") }?.let { item ->
                if (item.value.isEmpty()) {
                    showMessageDialog("请输入修正值!")
                    return
                }
                try {
                    val value = item.value.toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的修正值!")
                    return
                }
                sensorInfo.corrval = item.value
            }
        when (iotSensorType) {
            IOTSensorType.INCLINOMETER //测斜仪
            -> {
                //测段长
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("测段长") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入测段长!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的测段长!")
                            return
                        }
                        sensorInfo.spacing = item.value
                    }

                //模型切换
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamChooseItem>()
                    ?.findLast { it.name.contains("模型切换") }?.let { item ->
                        sensorInfo.model_type = modelTypeList.indexOf(item.value).toString()
                    }
            }

            IOTSensorType.RADAR_LEVEL_GAUGE //雷达液(物)位计 设置子雷达传感器型号
            -> {
                //雷达类型
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamChooseItem>()
                    ?.findLast { it.name.contains("雷达类型") }?.let { item ->
                        sensorInfo.child_type = childRadarTypeList.indexOf(item.value).toString()
                    }
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                //X轴初始角度
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("X轴初始角度") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入X轴初始角度!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的X轴初始角度!")
                            return
                        }
                        sensorInfo.initvalx = item.value
                    }
                //Y轴初始角度
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("Y轴初始角度") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入Y轴初始角度!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的Y轴初始角度!")
                            return
                        }
                        sensorInfo.initvaly = item.value
                    }
                //Z轴初始角度
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("Z轴初始角度") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入Z轴初始角度!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的Z轴初始角度!")
                            return
                        }
                        sensorInfo.initvalz = item.value
                    }
            }

            IOTSensorType.WEIR //量水堰计
            -> {
                //初始读数
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("初始读数") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入初始读数!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的初始读数!")
                            return
                        }
                        sensorInfo.lsycsds = item.value
                    }
                //初始堰上水头
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("初始堰上水头") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入初始堰上水头!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的初始堰上水头!")
                            return
                        }
                        sensorInfo.lsyysst = item.value
                    }
                //测站编码
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("测站编码") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入测站编码!")
                            return
                        }
                        sensorInfo.caddr = item.value
                    }
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER,//沉降仪
            -> {
                //初始值
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("初始值") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入初始值!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的初始值!")
                            return
                        }
                        sensorInfo.initval = item.value
                    }
            }

            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
            -> {
                //X轴初始值
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("X轴初始值") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入X轴初始值!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的X轴初始值!")
                            return
                        }
                        sensorInfo.initvalx = item.value
                    }
                //Y轴初始值
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("Y轴初始值") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入Y轴初始值!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的Y轴初始值!")
                            return
                        }
                        sensorInfo.initvaly = item.value
                    }
            }

            IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
            -> {
                //安装高程
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("安装高程") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入安装高程!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的安装高程!")
                            return
                        }
                        sensorInfo.tubealti = item.value
                    }
                //绳长
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                    ?.findLast { it.name.contains("绳长") }?.let { item ->
                        if (item.value.isEmpty()) {
                            showMessageDialog("请输入绳长!")
                            return
                        }
                        try {
                            val value = item.value.toDouble()
                        } catch (ex: Exception) {
                            showMessageDialog("请输入正确的绳长!")
                            return
                        }
                        sensorInfo.ropelen = item.value
                    }
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
            putString(AppContants.Extras.SENSOR_ADDR, sensorAddr)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }

}