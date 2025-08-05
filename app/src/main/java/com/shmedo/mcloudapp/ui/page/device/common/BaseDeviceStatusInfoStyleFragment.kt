package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.VibrateUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentBaseDeviceStatusInfoStyleBinding
import com.shmedo.mcloudapp.databinding.ItemDasSensorStatusBinding
import com.shmedo.mcloudapp.databinding.ItemDeviceStatusInfoBasicBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.DasSensorSubMonitorStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoTextSwitcherItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2024/5/13
 * 描述： TODO
 */
abstract class BaseDeviceStatusInfoStyleFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentBaseDeviceStatusInfoStyleBinding
    protected val toolbarViewModel: ToolbarViewModel by viewModels()
    protected val mStates: EmptyViewModel by viewModels()
    protected val iotParseManager: IOTParserManager by inject()
    protected val mdParseManager: MDParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_base_device_status_info_style,
            BR.stateVM,
            mStates
        ).addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    @CallSuper
    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBaseDeviceStatusInfoStyleBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                binding.refreshLayout.finishRefresh(false)
                return@onRefresh
            }
            queryStatusInfo()
        }
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<DeviceStatusInfoBasicItem>(R.layout.item_device_status_info_basic)
            addType<DeviceStatusInfoTextSwitcherItem>(R.layout.item_device_status_info_text_switcher)
            addType<DeviceStatusInfoSignalItem>(R.layout.item_device_status_info_signal)
            addType<DasSensorStatusInfo>(R.layout.item_das_sensor_status)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onCreate {
                when (itemViewType) {
                    R.layout.item_das_sensor_status -> {
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

                    else -> {}
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_device_status_info_basic -> {
                        val itemBinding = getBinding<ItemDeviceStatusInfoBasicBinding>()
                        val item = getModel<DeviceStatusInfoBasicItem>()
                        //必须要在事件发生之前就watch，如果你写在onLongClickListener中的话，就拿不到触摸点了，触摸事件被长按消费了
                        val builder = XPopup.Builder(context)
                            .hasShadowBg(false)
                            .watchView(itemBinding.tvClipboardValue)
                            .setPopupCallback(object : SimpleCallback() {})
                        itemBinding.tvClipboardValue.setOnLongClickListener {
                            VibrateUtils.vibrate(300)
                            builder.asAttachList(arrayListOf("复制").toTypedArray(), null)
                            { _, text ->
                                when (text) {
                                    "复制" -> {
                                        ClipboardUtils.copyText(item.value)
                                        Toaster.show("已复制到剪贴板")
                                    }
                                }
                            }
                                .show()
                            true
                        }
                    }

                    R.layout.item_das_sensor_status -> {
                        getModel<DasSensorStatusInfo>().let { info ->
                            //type 可能是 01，以 0 开头的数字，需要移除首个 0
                            val typeCode =
                                if (info.type.length > 1 && info.type.startsWith("0")) info.type.substring(
                                    1
                                ) else info.type
                            val sensorType = IOTSensorType.value(typeCode)
                            val addressText =
                                if (IOTSensorType.isVibratingSensor(sensorType)) "通道 ${info.addr + 1}"
                                else "地址 ${info.addr}"

                            val itemBinding = getBinding<ItemDasSensorStatusBinding>()
                            itemBinding.tvAddress.text = addressText
                            itemBinding.tvSensorName.text = sensorType.description
                            itemBinding.rvSubSensorData.models =
                                getSubMonitorStatusList(sensorType, info.valueList)
                        }
                    }

                    else -> {}
                }
            }
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_device_status_info_basic -> {
                        val item = getModel<DeviceStatusInfoBasicItem>()
                        processItemClick(item)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    protected open fun getSubMonitorStatusList(
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
                            monitorType = "温度（摄氏度）",
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
                            monitorType = "雨量（毫米）",
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
                            monitorType = "裂缝值（毫米）",
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
                            "温度（摄氏度）",
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
                            monitorType = "X轴（毫米）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴（毫米）",
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
                            monitorType = "空高值（毫米）",
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
                            monitorType = "水深（米）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "空管（米）",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水温（摄氏度）",
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
                            monitorType = "X轴角度（度）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴角度（度）",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Z轴角度（度）",
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
                            monitorType = "触发值（赫兹）",
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
                            monitorType = "液位值（毫米）",
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
                            monitorType = "沉降值（毫米）",
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
                            monitorType = "X轴数据（毫米）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "Y轴数据（毫米）",
                            monitorValue = dataList[1]
                        )
                    )
                }
            }

            IOTSensorType.WEATHER_STATION //气象计
            -> {
                if (dataList.isNotEmpty()) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "风速（米/秒）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "风向（度）",
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
                            monitorType = "温度（摄氏度）",
                            monitorValue = dataList[3]
                        )
                    )
                }
                if (dataList.size >= 5) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "气压（千帕）",
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
                            monitorType = "水深（毫米）",
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
                            monitorType = "溶氧率（毫克/升）",
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
                            monitorType = "温度（度）",
                            monitorValue = dataList[4]
                        )
                    )
                }
                if (dataList.size >= 6) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "氧化还原电位（毫伏）",
                            monitorValue = dataList[5]
                        )
                    )
                }
                if (dataList.size >= 7) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "叶绿素（微克）",
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
                            monitorType = "盐度（百分比）",
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
                            monitorType = "水位（米）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "温度（摄氏度）",
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
                            monitorType = "水深（米）",
                            monitorValue = dataList[0]
                        )
                    )
                }
                if (dataList.size >= 2) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "空管（米）",
                            monitorValue = dataList[1]
                        )
                    )
                }
                if (dataList.size >= 3) {
                    subMonitorStatusList.add(
                        DasSensorSubMonitorStatusItem(
                            monitorType = "水温（摄氏度）",
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
                            monitorType = "温度（摄氏度）",
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
                            monitorType = "温度（摄氏度）",
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

    protected open fun processItemClick(item: DeviceStatusInfoBasicItem) {}

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    protected open fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    protected open fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.QUERY_DEVICE_STATUS)
                || (commandType == IOTCommandType.MD_GET_DEVICE_STATUS)
                || (commandType == IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO)
                || (commandType == IOTCommandType.MD_GET_TERMINAL_ID)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }


    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initStatusInfo(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    protected open fun <T> initStatusInfo(content: T) {}


    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}