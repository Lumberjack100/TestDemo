package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

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
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSubSensorStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
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

class DasSensorInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasSensorInfoBinding
    private lateinit var mStates: DasSensorInfoViewModel
    private val iotParseManager: IOTParserManager by inject()

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
                        getSubMonitorStatusList(sensorType, info._val)
                }
            }
        }
    }

    private fun getSubMonitorStatusList(
        sensorType: IOTSensorType,
        data: String
    ): MutableList<DasSensorSubMonitorStatusItem> {
        val subMonitorStatusList: MutableList<DasSensorSubMonitorStatusItem> = arrayListOf()
        when (sensorType) {
            IOTSensorType.VIBRATING_SENSOR //振弦传感器
            -> {
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                val dataList = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
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
                        monitorValue = data
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

        var command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SENSOR_STATUS, "index=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询辅传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initInternalSensorData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询扩展传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
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

    private fun initInternalSensorData(content: String) {
        val decimalFormat = DecimalFormat("#.#")
        try {
            val info = MoshiUtil.fromJson<DasSubSensorStatusInfo>(content) ?: return
            //开关量传感器
            info.io?.let {
                mStates.isIOSensorVisible.set(true)
                mStates.ioType.set(it.type)
                mStates.ioValue.set(it.vaule.toString())
            }

            //数字水位计
            info.vwp?.let { vwpBean ->
                mStates.isVWPSensorVisible.set(true)
                mStates.vwpErrNo.set(vwpBean.errno.toString())
                val dataList = vwpBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    decimalFormat.applyPattern("#.###")
                    mStates.vwpValue1.set(dataList[0].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 2) {
                    decimalFormat.applyPattern("#.###")
                    mStates.vwpValue2.set(dataList[1].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 3) {
                    decimalFormat.applyPattern("#.###")
                    mStates.vwpValue3.set(dataList[2].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
            }

            //MEMS传感器
            info.mems?.let { memBean ->
                mStates.isMEMSSensorVisible.set(true)
                mStates.memsErrNo.set(memBean.errno.toString())

                val dataList = memBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                if (dataList.isNotEmpty()) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAxisX.set(dataList[0].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 2) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAxisY.set(dataList[1].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 3) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAxisZ.set(dataList[2].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 4) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAccelerationX.set(dataList[3].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 5) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAccelerationY.set(dataList[4].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
                if (dataList.size >= 6) {
                    decimalFormat.applyPattern("#.###")
                    mStates.memsAccelerationZ.set(dataList[5].toFloatOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: "--")
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initExternalSensorData(content: String) {
        try {
            val dataList = MoshiUtil.fromJson<List<DasSensorStatusInfo>>(content)
            if (dataList.isNullOrEmpty()) {
                mStates.isExternalSensorVisible.set(false)
                return
            }
            mStates.isExternalSensorVisible.set(true)
            binding.rvSensor.models = dataList
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = DasSensorInfoFragment()
    }
}