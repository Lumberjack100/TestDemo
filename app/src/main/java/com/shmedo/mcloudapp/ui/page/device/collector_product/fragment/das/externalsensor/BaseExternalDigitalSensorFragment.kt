package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

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
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentBaseExternalDigitalSensorBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKeyEmpty
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.ExternalDigitalSensorParamButtonItem
import com.shmedo.mcloudapp.model.ExternalDigitalSensorParamChooseItem
import com.shmedo.mcloudapp.model.ExternalDigitalSensorParamEditItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import timber.log.Timber

abstract class BaseExternalDigitalSensorFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentBaseExternalDigitalSensorBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var sensorListViewModel: DasExternalSensorListViewModel<DasExternalSensorInfo>

    protected val iotSensorType: IOTSensorType by lazy {
        IOTSensorType.Companion.getSensorTypeByCollectorCode(sensorListViewModel.collectorType.get())
    }
    private val usedAddressList = ArrayList<String>()
    private var sensorAddress = ""
    protected var sensorIndex: Int = -1
    private var sensorEditMode: Boolean = false


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
            sensorEditMode = it.getBoolean(SENSOR_EDIT_MODE, false)
            sensorIndex = it.getInt(AppContants.Extras.SENSOR_INDEX, -1)
            sensorAddress = it.getString(AppContants.Extras.SENSOR_ADDR, "")
        }
        usedAddressList.clear()
        sensorListViewModel.sensorModelMap.keys.filterNot { it == sensorAddress }
            .forEach { usedAddressList.add(it) }

        val externalSensorInfo = if (sensorListViewModel.sensorModelMap.containsKey(sensorAddress))
            sensorListViewModel.sensorModelMap[sensorAddress]!!
        else //新建传感器 采用第一个传感器的信息，没有则使用默认信息
            DasExternalSensorInfo(
                addr = "",
                type = iotSensorType.code,
                threshold = "",
                corrval = ""
            )

        initSensorInfo(externalSensorInfo)
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.setEnableRefresh(sensorEditMode)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            refreshData()
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
                ) { //静力水准/沉降仪 初始值重置
                    showResetInitValueWarningDialog()
                }
            }
            R.id.btn_submit.onClick {
                val item = getModel<ExternalDigitalSensorParamButtonItem>()
                KeyboardUtils.hideSoftInput(binding.root)

                if (item.btnText == "确定") {
                    checkValueIsValidAndUpdateSensor()

                } else if (item.btnText == "重置初始值") {
                    showResetInitValueWarningDialog()
                }
            }
        }
    }

    fun initSensorInfo(sensorInfo: DasExternalSensorInfo) {
        val groupList = mutableListOf<Any>()
        try {
            groupList.add(
                ExternalDigitalSensorParamEditItem(
                    name = "传感器地址",
                    value = if (sensorInfo.addr == "-1") "" else sensorInfo.addr,
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
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                }

                IOTSensorType.SOIL_MOISTURE //土壤含水率
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值(%rh)",
                            value = sensorInfo.threshold.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值(%rh)",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                }

                IOTSensorType.INCLINOMETER //测斜仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 1)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "测段长（毫米）",
                            value = sensorInfo.spacing.formatDoubleValue("", 1)
                        )
                    )
                    sensorInfo.model_type.notNullKeyEmpty { type ->
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
                                name = "测量间隔（毫秒）",
                                value = sensorInfo.measinval.formatDoubleValue("", 1),
                                inputEnable = false
                            )
                        )
                    }
                }

                IOTSensorType.ULTRASONIC_LEVEL_GAUGE, //超声波物位计
                IOTSensorType.RADAR_LEVEL_GAUGE //雷达物位计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 1)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "安装高程（米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
                    sensorInfo.child_type.notNullKeyEmpty { type ->
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
                            name = "触发值（度）",
                            value = sensorInfo.threshold.formatDoubleValue("", 2)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "X轴初始角度（度）",
                            value = sensorInfo.initvalx.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Y轴初始角度（度）",
                            value = sensorInfo.initvaly.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Z轴初始角度（度）",
                            value = sensorInfo.initvalz.formatDoubleValue("", 2),
                            desc = "初始值大于 360，设备将自动计算!"
                        )
                    )
                }

                IOTSensorType.INFRASOUND //次声
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（赫兹）",
                            value = sensorInfo.threshold.formatDoubleValue("", 2)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（赫兹）",
                            value = sensorInfo.corrval.formatDoubleValue("", 2)
                        )
                    )
                }

                IOTSensorType.WEIR //量水堰计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（立方米/秒）",
                            value = sensorInfo.threshold.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（毫米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 1),
                            desc = "修正浮子高度"
                        )
                    )
                    sensorInfo.lsycsds.notNullKeyEmpty {
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "初始读数（毫米）",
                                value = it.formatDoubleValue("", 1),
                                desc = "当初始读数设置值小于 0  时，设备将自动计算初始值!"
                            )
                        )
                    }
                    sensorInfo.lsyysst.notNullKeyEmpty {
                        groupList.add(
                            ExternalDigitalSensorParamEditItem(
                                name = "初始堰上水头（毫米）",
                                value = it.formatDoubleValue("", 1),
                                desc = "当水经堰顶点流出时，设置值为堰顶点到水面的距离；否则，设置值为堰顶点到浮子距离的负值"
                            )
                        )
                    }
                    sensorInfo.caddr.notNullKeyEmpty {
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
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（毫米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "初始值（毫米）",
                            value = sensorInfo.initval.formatDoubleValue("", 3),
                            btnVisible = sensorEditMode //只有在编辑传感器下才显示重置按钮，新建传感器不显示
                        )
                    )
                }

                IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "X轴初始值（毫米）",
                            value = sensorInfo.initvalx.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "Y轴初始值（毫米）",
                            value = sensorInfo.initvaly.formatDoubleValue("", 3),
                        )
                    )
                    if (sensorEditMode) {
                        groupList.add(
                            GapItem(
                                height = ConvertUtils.dp2px(100f)
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
                            name = "触发值（米/秒）",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（米/秒）",
                            value = sensorInfo.corrval.formatDoubleValue("", 3),
                        )
                    )
                }

                IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
                -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值（毫米）",
                            value = sensorInfo.threshold.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值（毫米）",
                            value = sensorInfo.corrval.formatDoubleValue("", 1),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "安装高程（米）",
                            value = sensorInfo.tubealti.formatDoubleValue("", 3)
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "绳长（米）",
                            value = sensorInfo.ropelen.formatDoubleValue("", 3)
                        )
                    )
                }

                else -> {
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "触发值",
                            value = sensorInfo.threshold.formatDoubleValue("", 3),
                        )
                    )
                    groupList.add(
                        ExternalDigitalSensorParamEditItem(
                            name = "修正值",
                            value = sensorInfo.corrval.formatDoubleValue("", 3)
                        )
                    )
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
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
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
        sensorInfo.corrval = IOTConstants.NULL_KEY
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
                sensorInfo.spacing = IOTConstants.NULL_KEY
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
                sensorInfo.model_type = IOTConstants.NULL_KEY
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamChooseItem>()
                    ?.findLast { it.name.contains("模型切换") }?.let { item ->
                        sensorInfo.model_type = modelTypeList.indexOf(item.value).toString()
                    }
            }

            IOTSensorType.RADAR_LEVEL_GAUGE //雷达液(物)位计 设置子雷达传感器型号
            -> {
                //安装高程
                sensorInfo.corrval = IOTConstants.NULL_KEY
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
                        sensorInfo.corrval = item.value
                    }

                //雷达类型
                sensorInfo.child_type = IOTConstants.NULL_KEY
                binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamChooseItem>()
                    ?.findLast { it.name.contains("雷达类型") }?.let { item ->
                        sensorInfo.child_type = childRadarTypeList.indexOf(item.value).toString()
                    }
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                //X轴初始角度
                sensorInfo.initvalx = IOTConstants.NULL_KEY
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
                sensorInfo.initvaly = IOTConstants.NULL_KEY
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
                sensorInfo.initvalz = IOTConstants.NULL_KEY
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
                sensorInfo.lsycsds = IOTConstants.NULL_KEY
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
                sensorInfo.lsyysst = IOTConstants.NULL_KEY
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
                sensorInfo.caddr = IOTConstants.NULL_KEY
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
                sensorInfo.initval = IOTConstants.NULL_KEY
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
                sensorInfo.initvalx = IOTConstants.NULL_KEY
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
                sensorInfo.initvaly = IOTConstants.NULL_KEY
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
                sensorInfo.tubealti = IOTConstants.NULL_KEY
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
                sensorInfo.ropelen = IOTConstants.NULL_KEY
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
        if (sensorListViewModel.sensorModelMap.containsKey(sensorInfo.addr)) {
            sensorListViewModel.sensorModelMap.remove(sensorInfo.addr)
        }
        sensorListViewModel.sensorModelMap[sensorInfo.addr] = sensorInfo
        sensorListViewModel.updateIsRefreshSensorList(true)

        nav().navigateUp()
    }

    private fun showResetInitValueWarningDialog() {
        val address =
            binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                ?.findLast { it.name.contains("传感器地址") }?.value?.trim() ?: ""

        showMessage(
            "确定重置初始值吗？",
            "温馨提示",
            "确定",
            {
                //垂线坐标仪 初始值重置
                if (address.isEmpty()) {
                    Toaster.show("传感器地址不能为空!")
                    return@showMessage
                }
                resetInitValue()
            },
            "取消"
        )
    }

    /**
     * 重置 静力水准/沉降仪/垂线坐标仪 初始值
     */
    protected open fun resetInitValue() {}

    protected open fun refreshData() {}

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val SENSOR_EDIT_MODE = "sensor_edit_mode"

        fun newBundleArguments(
            sensorEditMode: Boolean = false,
            index: Int,
            sensorAddress: String = "",
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putBoolean(SENSOR_EDIT_MODE, sensorEditMode)
            putInt(AppContants.Extras.SENSOR_INDEX, index)
            putString(AppContants.Extras.SENSOR_ADDR, sensorAddress)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(
                AppContants.Extras.STATUS_BAR_COLOR,
                statusBarColor
            )
        }
    }
}