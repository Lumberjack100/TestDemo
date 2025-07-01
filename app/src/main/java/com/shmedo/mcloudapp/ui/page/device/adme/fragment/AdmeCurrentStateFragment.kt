package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.VibrateUtils
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.shmedo.lib.cmd.base.iot_cmd.enums.AdmeCTRMotionState
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeCurrentStateBinding
import com.shmedo.mcloudapp.databinding.ItemDeviceStatusInfoBasicBinding
import com.shmedo.mcloudapp.extensions.getAdmeErrorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeCurrentStateViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 运行状态页面
 *
 */
class AdmeCurrentStateFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeCurrentStateBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdmeCurrentStateViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val deviceAbnormalList: ArrayList<String> = ArrayList()



    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_adme_current_state, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeCurrentStateBinding
        binding.llToolbar.toolbar.title = "运行状态"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
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
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryEquipmentState()
        }
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<DeviceStatusInfoBasicItem>(R.layout.item_device_status_info_basic)
            addType<DeviceStatusInfoSignalItem>(R.layout.item_device_status_info_signal)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onBind {
                when (itemViewType) {
                    R.layout.item_device_status_info_basic -> {
                        val itemBinding = getBinding<ItemDeviceStatusInfoBasicBinding>()
                        val item = getModel<DeviceStatusInfoBasicItem>()
                        //必须要在事件发生之前就watch，如果你写在onLongClickListener中的话，就拿不到触摸点了，触摸事件被长按消费了
                        val builder = XPopup.Builder(context)
                            .hasShadowBg(false)
                            .watchView(itemBinding.tvValue)
                            .setPopupCallback(object : SimpleCallback() {
                                override fun onClickOutside(popupView: BasePopupView?) {
                                    item.refreshClipboardState(false)
                                }
                            })
                        itemBinding.tvValue.setOnLongClickListener {
                            item.refreshClipboardState(true)
                            VibrateUtils.vibrate(300)
                            builder.asAttachList(arrayListOf("复制").toTypedArray(), null)
                            { _, text ->
                                when (text) {
                                    "复制" -> {
                                        item.refreshClipboardState(false)
                                        ClipboardUtils.copyText(item.value)
                                        Toaster.show("已复制到剪贴板")
                                    }
                                }
                            }
                                .show()
                            true
                        }
                    }

                    else -> {

                    }
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
            // 可选项, 粘性监听器
            onHoverAttachListener = object : OnHoverAttachListener {
                override fun attachHover(v: View) {
                    ViewCompat.setElevation(v, 10F) // 悬停时显示阴影
                }

                override fun detachHover(v: View) {
                    ViewCompat.setElevation(v, 0F) // 非悬停时隐藏阴影
                }
            }
        }
    }

    private fun processItemClick(item: DeviceStatusInfoBasicItem) {
        if (item.isClickable && item.name == "设备状态" && item.value == "异常") {
            showErrorModulesInfoDialog()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的当前状态
     */
    private fun queryEquipmentState() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE)
        commandItems.add(command)

        if (productType == ProductType.ADME_HAC) {
            command = IOTCommandUtil.getCommand(IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE)
            commandItems.add(command)
        } else {
            command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE)
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE -> {
                val result =
                    iotParseManager.parse<AdmeCurrentStateInfo>(
                        cmdStr,
                        IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {//获取 ADME 的运行状态
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {//获取ADME HAC 的运行状态
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        updateHacMotionState(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initStatusInfo(stateInfo: AdmeCurrentStateInfo) {
        launchWithViewLifecycle {
            try {
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("基本信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备型号",
                    value = if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { stateInfo.productid },
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备IMEI",
                    value = stateInfo.imeid,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备ICCID",
                    value = stateInfo.simid,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "CTR固件版本",
                    value = stateInfo.firversion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "力矩驱动器固件版本",
                    value = stateInfo.motorrv,
                )
                stateInfo.scsq.notNullKey {
                    val temp = it.toIntOrNull() ?: 0
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "4G信号强度",
                            signalValue = if (temp <= 0)
                                temp
                            else
                                temp * 2 - 113
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("设备工作信息"))
                stateInfo.abndiasis.notNullKey {
                    deviceAbnormalList.clear()
                    deviceAbnormalList.addAll(DeviceStatusHelper.checkAdmeDeviceAbnormal(it))
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备状态",
                            value = if (deviceAbnormalList.isEmpty()) "正常" else "异常",
                            textColorRes = if (deviceAbnormalList.isEmpty()) ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ) else ColorUtils.getColor(R.color.error_FF4400),
                            isClickable = deviceAbnormalList.isNotEmpty()
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "工作模式",
                    value = if (stateInfo.testway == "0") "常规测量模式" else if (stateInfo.testway == "1") "特定点位模式" else if (stateInfo.testway == "2") "静态测量模式" else "设备停用模式",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "CTR输入电压",
                    value = stateInfo.ctrinputv,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "驱动器输入电压",
                    value = stateInfo.driveinputv,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "设备温度",
                    value = stateInfo.temperature,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "设备湿度",
                    value = stateInfo.humidity,
                    defaultValue = "0",
                    digit = 2,
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备下降次数",
                    value = stateInfo.downnum,
                )
                stateInfo.runmileage.notNullKey {
                    val tempValue = it.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "钢丝绳运行里程",
                            value = DeviceStatusInfoProcessor.formatDoubleValue(
                                tempValue.toString(),
                                "0",
                                3
                            ) + " m"
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "竖向磁开关触发次数",
                    value = stateInfo.verticalswitchnum,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "旋转磁开关触发次数",
                    value = stateInfo.rotaryswitchnum,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "刹车片启闭次数",
                    value = stateInfo.brakepadnum,
                )
                stateInfo.nexttime.notNullKey {
                    val tempValue = it.toULongOrNull() ?: 0u
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "预计下次测量时间",
                            value = if (tempValue > 0u) TimeUtils.millis2String(
                                tempValue.toLong(),
                                "yyyy-MM-dd HH:mm"
                            ) else ""
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("测斜仪信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "测斜仪类型",
                    value = if (stateInfo.inctype == "0") "433测斜仪" else "蓝牙测斜仪",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "测斜仪信道号",
                    value = stateInfo.incnum,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "测斜仪位置信息",
                    value = stateInfo.incloc,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "测斜仪电压",
                    value = stateInfo.incvoltage,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "测斜仪管温度",
                    value = stateInfo.intertempe,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                stateInfo.bcsq.notNullKey {
                    val temp = it.toIntOrNull() ?: 0
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "测斜仪蓝牙信号强度",
                            signalValue = if (temp <= 0)
                                temp
                            else
                                temp * 2 - 113
                        )
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        try {
            if (admeMotionState.measmode.isEmpty()) {
                mStates.ctrMotionInfoVisible.set(false)
                return
            }
            mStates.ctrMotionInfoVisible.set(true)
            updateMotionInfo(
                admeMotionState.measmode,
                admeMotionState.measpoint,
                admeMotionState.motorinfo,
                admeMotionState.waittime
            )
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun updateHacMotionState(hacMotionState: HacMotionState) {
        try {
            if (hacMotionState.measmode.isEmpty()) {
                mStates.ctrMotionInfoVisible.set(false)
                return
            }
            mStates.ctrMotionInfoVisible.set(true)
            //CTR 工作异常
            if (hacMotionState.abndiasis != "0") {
                //列出异常原因
                val errorMsg = getAdmeErrorMsg(hacMotionState.abndiasis, delimiters = ";")
                if (errorMsg.isEmpty()) {
                    return
                }
                mStates.measureMode.set("异常保护")
                mStates.isMotorInfoNormal.set(false)
                mStates.motorInfo.set("异常原因: $errorMsg")
                return
            }
            updateMotionInfo(
                hacMotionState.measmode,
                hacMotionState.measpoint,
                hacMotionState.motorinfo,
                hacMotionState.waittime
            )

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun updateMotionInfo(
        measmode: String,
        measurePoint: String,
        motorinfo: String,
        waittime: String
    ) {
        mStates.isMotorInfoNormal.set(true)
        mStates.measureMode.set(if (measmode == "0") "正测" else "反测")
        when (val ctrMotionState = AdmeCTRMotionState.valueByCode(motorinfo)) {
            AdmeCTRMotionState.DOWN -> {//测斜仪下放
                val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    String.format("测斜仪下放: %s 米", measurePoint)
                else
                    "测斜仪下放"
                mStates.motorInfo.set(msg)
            }

            AdmeCTRMotionState.BOTTOM_WAITING -> {//管底等待
                val msg = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    String.format(
                        "管底等待-位置(%s 米)-剩余时间(%s 秒)",
                        measurePoint,
                        waittime
                    )
                else
                    "管底等待"
                mStates.motorInfo.set(msg)
            }

            AdmeCTRMotionState.POINT_MEASUREMENT -> {//测点测量
                if (measurePoint.isNotEmpty() && measurePoint.contains("|")) {
                    val points = measurePoint.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    val msg =
                        if (points.size > 1 && points[0].isNotEmpty() && points[1].isNotEmpty())
                            String.format(
                                "测点测量-测斜仪位置(%s 米)-测点序列(%s)",
                                points[1],
                                points[0]
                            )
                        else
                            "测点测量"
                    mStates.motorInfo.set(msg)
                } else
                    mStates.motorInfo.set("测点测量")
            }

            else -> {
                mStates.motorInfo.set(ctrMotionState.description)
            }
        }
    }

    private fun showErrorModulesInfoDialog() {
        if (deviceAbnormalList.isEmpty()) {
            Toaster.show("设备异常信息为空")
            return
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asCenterList(
                "异常信息", deviceAbnormalList.toTypedArray(),
                null, -1,
                null, 0, R.layout.custom_xpopup_adapter_abnormal_info
            )
            .show()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}