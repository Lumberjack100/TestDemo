package com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.MDRainStation
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoThree
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoTwo
import com.shmedo.lib.device.base.md_cmd.model.das.InclinometerInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentDasSensorInfoBinding
import com.shmedo.mcloudapp.databinding.ItemDasSensorStatusBinding
import com.shmedo.mcloudapp.device.model.DasSensorSubMonitorStatusItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasSensorInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasSensorInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasSensorInfoBinding
    private lateinit var mStates: DasSensorInfoViewModel
    private val mdParseManager: MDParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_sensor_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasSensorInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
        initSensorAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryInfo()
        }
    }

    private fun initSensorAdapter() {
        binding.rvSensor.setup { rv ->
            rv.layoutManager = LinearLayoutManager(context)
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(8f), ColorUtils.getColor(
                        R.color.transparent
                    )
                )
            )
            addType<DasSensorStatusInfo>(R.layout.item_das_sensor_status)
            onCreate {
                val itemBinding = getBinding<ItemDasSensorStatusBinding>()
                itemBinding.rvSubSensorData.setup { subRv ->
                    subRv.addItemDecoration(
                        MyGridSpacingItemDecoration(
                            2,
                            ConvertUtils.dp2px(8f), false
                        )
                    )
                    addType<DasSensorSubMonitorStatusItem>(R.layout.item_das_sensor_sub_monitor_status)
                }
            }
            onBind {
                getModel<DasSensorStatusInfo>().let { info ->
                    //type 可能是 01，以 0 开头的数字，需要移除首个 0
                    val typeCode =
                        if (info.type.length > 1 && info.type.startsWith("0")) info.type.substring(1) else info.type
                    val sensorType = IOTSensorType.value(typeCode)

                    val itemBinding = getBinding<ItemDasSensorStatusBinding>()
                    itemBinding.tvAddress.text =
                        String.format("通道 %s", (info.addr + 1).toString())
                    itemBinding.tvSensorName.text = sensorType.description
                    itemBinding.rvSubSensorData.models =
                        getSubMonitorStatusList(sensorType, info._val)
                }
            }
        }
    }

    private fun getSubMonitorStatusList(
        sensorType: IOTSensorType,
        data: String
    ): MutableList<DasSensorSubMonitorStatusItem> {
        val decimalFormat = DecimalFormat("#.###")
        val subMonitorStatusList: MutableList<DasSensorSubMonitorStatusItem> = arrayListOf()
        when (sensorType) {
            IOTSensorType.VIBRATING_SENSOR //振弦传感器
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "模数",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.RAIN_GAUGE //雨量计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "雨量(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.WIRE_SHIFT //拉绳式裂缝计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "裂缝值(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.SOIL_MOISTURE //土壤含水率
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "湿度(%RH)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.INCLINOMETER //测斜仪
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "X轴(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "Y轴(毫米)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.ULTRASONIC_LEVEL_GAUGE, //超声波物位计
            IOTSensorType.RADAR_LEVEL_GAUGE //雷达物位计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "空高值(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.UPLIFT_PRESSURE_GAUGE //扬压力计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水深(m)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "空管(m)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水温(℃)",
                            dataList[2].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "X轴角度(°)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "Y轴角度(°)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.INFRASOUND //次声传感器
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "触发值(单位:Hz)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.WEIR //量水堰
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "液位值(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
//                if (dataList.size >= 2) {
//                    subMonitorStatusList.add(
//                        DasSensorSubMonitorStatusItem(
//                            "Y轴角度(°)",
//                            dataList[1]
//                        )
//                    )
//                }
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER ,//沉降仪
            IOTSensorType.VERTICAL_COORDINATE ,//垂线坐标仪
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "沉降值(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.WEATHER_STATION //气象站
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "风速(m/s)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "风向(°)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "湿度(%RH)",
                            dataList[2].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 4) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[3].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 5) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "气压(KPa)",
                            dataList[4].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.TURBIDITY_METER //浊度仪传感器
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "浊度(NTU)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水深(毫米)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.WATER_QUALITY_METER //多参数水质仪
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "溶氧率(mg/L)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "浊度(NTU)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "电导率(uS/cm)",
                            dataList[2].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 4) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "酸碱度(pH)",
                            dataList[3].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 5) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(°)",
                            dataList[4].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 6) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "氧化还原电位(mV)",
                            dataList[5].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 7) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "叶绿素(ug)",
                            dataList[6].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 8) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "藻蓝蛋白(Kcells/mL)",
                            dataList[7].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 9) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "盐度(%)",
                            dataList[8].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.WATER_LEVEL_GAUGE //水位(液位)计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水位(m)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.KANG_PERCOLATE, //基康渗压计
            IOTSensorType.GUDAN_PERCOLATE //葛南渗压计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水深(m)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "空管(m)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "水温(℃)",
                            dataList[2].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.GUDAN_STRESS //葛南应变计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "应变(μ)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            IOTSensorType.JUNXING_ZLJ_300T //轴力计
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "轴力(KN)",
                            dataList[0].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            dataList[1].toDoubleOrNull()?.let {
                                decimalFormat.format(it)
                            } ?: "--"
                        )
                    )
                }
            }

            else -> {
                subMonitorStatusList.add(
                    DasSensorSubMonitorStatusItem(
                        "数值",
                        data.toDoubleOrNull()?.let {
                            decimalFormat.format(it)
                        } ?: "--"
                    )
                )
            }
        }
        return subMonitorStatusList
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        /**
         * 查询设备状态2:##042\r\n<br/>
         * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n<br/>
         * （1）SN号<br/>
         * （2）经度<br/>
         * （3）纬度<br/>
         * （4）设备内部电压<br/>
         * （5）设备外部电压<br/>
         * （6）太阳能控制器状态<br/>
         * （7）太阳能板电压<br/>
         * （8）电池电压<br/>
         * （9）日发电量<br/>
         * （10）日耗电量<br/>
         * （11）机箱内部温湿度状态<br/>
         * （12）机箱内部温度<br/>
         * （13）机箱内部湿度<br/>
         * （14）机箱外部温湿度状态<br/>
         * （15）机箱外部温度<br/>
         * （16）机箱外部湿度<br/>
         * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器<br/>
         * （18）降雨量或断线报警器状态(1:断开，0：闭合)<br/>
         * 示例：$$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0<br/>
         */
        var command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_2)
        commandItems.add(command)

        //查询倾角计信息
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_INCLINOMETER_INFO)
        commandItems.add(command)

        /**
         * 获取主传感器状态:##043\r\n<br/>
         * 应答:$$043,(1),(2),(3),(4),(5)<br/>
         * （1）SN号<br/>
         * （2）采集器型号<br/>
         * （3）采集器地址(当采集器地址为0时，关闭采集功能)<br/>
         * （4）传感器状态，用冒号分隔的字符串<br/>
         * ①:②:③，其中 ①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据<br/>
         * （5）传感器状态，和（2）格式相同，<br/>
         * 注：传感器状态可能有很多个，有接入传感器个数决定。<br/>
         */
        command =
            MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_3)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_2 -> {//##042\r\n：查询设备状态2
                val result = mdParseManager.parse<DeviceStatusInfoTwo>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_2
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询开关量传感器信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        setDeviceStatusTwo(result.data)
                    }
                }
            }

            MDCommandType.QUERY_INCLINOMETER_INFO -> {//##046
                val result = mdParseManager.parse<InclinometerInfo>(
                    cmdStr,
                    MDCommandType.QUERY_INCLINOMETER_INFO
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询倾角计信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        setInclinometerInfo(result.data)
                    }
                }
            }

            MDCommandType.QUERY_DAS_STATUS_3 -> {//##043\r\n: 获取主传感器状态
                val result = mdParseManager.parse<DeviceStatusInfoThree>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_3
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询扩展传感器状态出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initExternalSensorData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }


    private fun setDeviceStatusTwo(info: DeviceStatusInfoTwo) {
        try {
            decimalFormat.applyPattern("#.#")
            when (MDRainStation.value(info.switchType)) {
                MDRainStation.CLOSE -> {//2：关闭
                    mStates.isIOSensorVisible.set(false)
                }

                MDRainStation.RAIN_OPEN -> {//1：雨量计
                    mStates.isIOSensorVisible.set(true)
                    mStates.ioType.set(1)
                    mStates.ioValue.set(info.rainfallStatus.toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "")
                }

                MDRainStation.ALARM_OPEN -> {//3：断线报警器
                    mStates.isIOSensorVisible.set(true)
                    mStates.ioType.set(3)
                    mStates.ioValue.set(if (info.rainfallStatus == "1" || info.rainfallStatus == "1.0") "1" else "0")
                }
            }

        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 设置倾角计信息
     */
    private fun setInclinometerInfo(info: InclinometerInfo) {
        if (info.status.isNullOrEmpty()) {
            mStates.isMEMSSensorVisible.set(false)
            return
        }
        try {
            mStates.isMEMSSensorVisible.set(true)
            mStates.memsErrNo.set(info.status)

            decimalFormat.applyPattern("#.###")
            mStates.memsAxisX.set(info.xAxis.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")
            mStates.memsAxisY.set(info.yAxis.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")
            mStates.memsAxisZ.set(info.zAxis.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")

            mStates.memsAccelerationX.set(info.xAcceleration.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")
            mStates.memsAccelerationY.set(info.yAcceleration.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")
            mStates.memsAccelerationZ.set(info.zAcceleration.toFloatOrNull()?.let {
                decimalFormat.format(it)
            } ?: "--")
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initExternalSensorData(info: DeviceStatusInfoThree) {
        try {
            val dataList = mutableListOf<DasSensorStatusInfo>()
            if (info.collectorAddress == "0" || info.sensorStatus.isNullOrEmpty()) {
                mStates.isExternalSensorVisible.set(false)
                return
            }

            mStates.isExternalSensorVisible.set(true)
            info.sensorStatus.forEach { tempStr ->
                //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据
                val sensorStatus = tempStr.split(":").dropLastWhile { it.isEmpty() }
                if (sensorStatus.size < 3) {
                    return@forEach
                }
                val addr = sensorStatus[0].toIntOrNull() ?: 0
                val status = sensorStatus[1].toIntOrNull() ?: 0
                val value = sensorStatus[2]
                dataList.add(
                    DasSensorStatusInfo(
                        type = info.collectorModel,
                        addr = addr,
                        errno = status,
                        _val = value
                    )
                )
            }
            binding.rvSensor.models = dataList

        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = BleDasSensorInfoFragment()
    }
}