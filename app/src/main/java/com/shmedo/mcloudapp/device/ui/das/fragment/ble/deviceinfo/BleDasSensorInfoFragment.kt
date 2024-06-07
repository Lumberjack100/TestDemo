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
                    val addressText = String.format("地址 %s", info.addr.toString())

                    val itemBinding = getBinding<ItemDasSensorStatusBinding>()
                    itemBinding.tvAddress.text = addressText
                    itemBinding.tvSensorName.text = sensorType.description
                    itemBinding.rvSubSensorData.models =
                        getSubMonitorStatusList(sensorType, info.valueList)
                }
            }
        }
    }

    private fun getSubMonitorStatusList(
        sensorType: IOTSensorType,
        dataList: List<String>
    ): MutableList<DasSensorSubMonitorStatusItem> {
        val subMonitorStatusList: MutableList<DasSensorSubMonitorStatusItem> = arrayListOf()
        when (sensorType) {
            IOTSensorType.VIBRATING_SENSOR //振弦传感器
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(℃)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "模数",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.RAIN_GAUGE //雨量计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "雨量(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.WIRE_SHIFT //拉绳式裂缝计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "裂缝值(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.SOIL_MOISTURE //土壤含水率
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "温度(℃)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            "湿度(%RH)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.INCLINOMETER //测斜仪
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "X轴(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴(毫米)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.ULTRASONIC_LEVEL_GAUGE, //超声波物位计
            IOTSensorType.RADAR_LEVEL_GAUGE //雷达物位计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "空高值(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.UPLIFT_PRESSURE_GAUGE //扬压力计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水深(m)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "空管(m)",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水温(℃)",
                            monitorValue = dataList[2]
                        )
                    )
                }
            }

            IOTSensorType.LUYAN_INCLINOMETER //倾角仪
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "X轴角度(°)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴角度(°)",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Z轴角度(°)",
                            monitorValue = dataList[2]
                        )
                    )
                }
            }

            IOTSensorType.INFRASOUND //次声传感器
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "触发值(单位:Hz)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.WEIR //量水堰
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "液位值(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.STATIC_LEVEL,//静力水准
            IOTSensorType.SEDIMENTATION_METER,//沉降仪
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "沉降值(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "X轴数据(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴数据(毫米)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.WEATHER_STATION //气象站
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "风速(m/s)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "风向(°)",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "湿度(%RH)",
                            monitorValue = dataList[2]
                        )
                    )
                }
                if (dataList.size >= 4) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(℃)",
                            monitorValue = dataList[3]
                        )
                    )
                }
                if (dataList.size >= 5) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "气压(KPa)",
                            monitorValue = dataList[4]
                        )
                    )
                }
            }

            IOTSensorType.TURBIDITY_METER //浊度仪传感器
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "浊度(NTU)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE //数字式水位计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水深(毫米)",
                            monitorValue = dataList[0]
                        )
                    )
                }
            }

            IOTSensorType.WATER_QUALITY_METER //多参数水质仪
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "溶氧率(mg/L)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "浊度(NTU)",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "电导率(uS/cm)",
                            monitorValue = dataList[2]
                        )
                    )
                }
                if (dataList.size >= 4) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "酸碱度(pH)",
                            monitorValue = dataList[3]
                        )
                    )
                }
                if (dataList.size >= 5) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(°)",
                            monitorValue = dataList[4]
                        )
                    )
                }
                if (dataList.size >= 6) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "氧化还原电位(mV)",
                            monitorValue = dataList[5]
                        )
                    )
                }
                if (dataList.size >= 7) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "叶绿素(ug)",
                            monitorValue = dataList[6]
                        )
                    )
                }
                if (dataList.size >= 8) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "藻蓝蛋白(Kcells/mL)",
                            monitorValue = dataList[7]
                        )
                    )
                }
                if (dataList.size >= 9) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "盐度(%)",
                            monitorValue = dataList[8]
                        )
                    )
                }
            }

            IOTSensorType.WATER_LEVEL_GAUGE //水位(液位)计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水位(m)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(℃)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.KANG_PERCOLATE, //基康渗压计
            IOTSensorType.GUDAN_PERCOLATE //葛南渗压计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水深(m)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "空管(m)",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水温(℃)",
                            monitorValue = dataList[2]
                        )
                    )
                }
            }

            IOTSensorType.GUDAN_STRESS //葛南应变计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "应变(μ)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(℃)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.JUNXING_ZLJ_300T //轴力计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "轴力(KN)",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度(℃)",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            else -> {
                subMonitorStatusList.add(
                    DasSensorSubMonitorStatusItem(
                        monitorType = "数值",
                        monitorValue = dataList.joinToString(",")
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
                val tempList = tempStr.split(":").dropLastWhile { it.isEmpty() }
                if (tempList.size < 3) {
                    return@forEach
                }
                dataList.add(
                    DasSensorStatusInfo(
                        type = info.collectorModel,
                        addr = tempList[0].toIntOrNull() ?: 0,
                        errno = tempList[1].toIntOrNull() ?: 0,
                        valueList = tempList.subList(2, tempList.size)
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