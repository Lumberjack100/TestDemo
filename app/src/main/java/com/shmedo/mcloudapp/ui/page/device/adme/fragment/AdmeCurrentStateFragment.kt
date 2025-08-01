package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.AdmeCTRMotionState
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.getAdmeErrorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.AdmeMotionStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/1/25
 * @desc: 优化后的 ADME 运行状态页面
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyle2Fragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 将 CTR 运动状态作为 RecyclerView 的一个项目实现
 * 5. 保持原有的ADME特定业务逻辑不变
 * 6. 支持4G和蓝牙两种通讯方式
 */
class AdmeCurrentStateFragment : OptimizedBaseDeviceStatusInfoStyle2Fragment() {

    private val deviceAbnormalList: ArrayList<String> = ArrayList()
    private var admeMotionStatusItem: AdmeMotionStatusItem = AdmeMotionStatusItem()

    @CallSuper
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "运行状态"

        // 扩展适配器支持 AdmeMotionStatusItem
        binding.recyclerview.bindingAdapter.addType<AdmeMotionStatusItem>(R.layout.item_adme_motion_status)
    }

    override fun processItemClick(item: DeviceStatusInfoBasicItem) {
        super.processItemClick(item)
        if (item.isClickable && item.name == "设备状态" && item.value == "异常") {
            showErrorModulesInfoDialog()
        }
    }

    /**
     * 查询设备状态信息
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 根据产品类型获取运动状态
        if (productType == ProductType.ADME_HAC) {
            commands.add(IOTCommandUtil.getCommand(IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE))
        } else {
            commands.add(IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE))
        }

        // 获取设备基本状态
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE))

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 状态查询失败显示Dialog
            )
        )
    }

    /**
     * 处理指令响应 - 统一的指令响应处理
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE -> {
                val result = iotParseManager.parse<AdmeCurrentStateInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取CTR工作状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        updateHacMotionState(result.data)
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    /**
     * 初始化状态信息 - 处理ADME设备状态数据
     */
    override fun <T> initStatusInfo(content: T) {
        val stateInfo = content as AdmeCurrentStateInfo
        launchWithViewLifecycle {
            try {
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 添加 CTR 运动状态作为第一个项目
                groupList.add(admeMotionStatusItem)
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

                // 基本信息组
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
                    isClipboard = true
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备ICCID",
                    value = stateInfo.simid,
                    isClipboard = true
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

                // 4G信号强度
                stateInfo.scsq.notNullKey {
                    var temp = it.toIntOrNull() ?: 0
                    temp = if (temp <= 0) temp else temp * 2 - 113
                    if (temp !in -110..-50) {
                        temp = 0
                    }

                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "4G信号强度",
                            signalValue = temp,
                            textColorRes = if (temp == 0) ColorUtils.getColor(
                                R.color.error_FF4400
                            ) else 0,
                            isBottomItem = true
                        )
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

                // 设备工作信息组
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
                    value = when (stateInfo.testway) {
                        "0" -> "常规测量模式"
                        "1" -> "特定点位模式"
                        "2" -> "静态测量模式"
                        else -> "设备停用模式"
                    },
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
                            ) else "",
                            isBottomItem = true
                        )
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

                // 测斜仪信息组
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
                    isBottomItem = true
                )

                stateInfo.bcsq.notNullKey {
                    var temp = it.toIntOrNull() ?: 0
                    temp = if (temp <= 0) temp else temp * 2 - 113
                    if (temp !in -110..-50) {
                        temp = 0
                    }
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "测斜仪蓝牙信号强度",
                            signalValue = temp,
                            textColorRes = if (temp == 0) ColorUtils.getColor(
                                R.color.error_FF4400
                            ) else 0,
                            isBottomItem = true
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

    /**
     * 更新 ADME 运动状态
     */
    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        try {
            if (admeMotionState.measmode.isEmpty()) {
                admeMotionStatusItem.hideMotionStatus()
                return
            }
            admeMotionStatusItem.showMotionStatus()
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

    /**
     * 更新 HAC 运动状态
     */
    private fun updateHacMotionState(hacMotionState: HacMotionState) {
        try {
            if (hacMotionState.measmode.isEmpty()) {
                admeMotionStatusItem.hideMotionStatus()
                return
            }
            admeMotionStatusItem.showMotionStatus()

            // CTR 工作异常
            if (hacMotionState.abndiasis != "0") {
                // 列出异常原因
                val errorMsg = getAdmeErrorMsg(hacMotionState.abndiasis, delimiters = ";")
                if (errorMsg.isEmpty()) {
                    return
                }
                admeMotionStatusItem.refreshMotionStatus(
                    visible = true,
                    isNormal = false,
                    mode = "异常保护",
                    info = "异常原因: $errorMsg"
                )
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

    /**
     * 更新运动信息
     */
    private fun updateMotionInfo(
        measmode: String,
        measurePoint: String,
        motorinfo: String,
        waittime: String
    ) {
        val mode = if (measmode == "0") "正测" else "反测"
        var info = ""
        var point = ""

        when (val ctrMotionState = AdmeCTRMotionState.valueByCode(motorinfo)) {
            AdmeCTRMotionState.DOWN -> {//测斜仪下放
                info = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    "测斜仪下放: $measurePoint 米"
                else
                    "测斜仪下放"
            }

            AdmeCTRMotionState.BOTTOM_WAITING -> {//管底等待
                info = if (measurePoint.isNotEmpty() && !measurePoint.contains("|"))
                    "管底等待-位置($measurePoint 米)-剩余时间($waittime 秒)"
                else
                    "管底等待"
            }

            AdmeCTRMotionState.POINT_MEASUREMENT -> {//测点测量
                if (measurePoint.isNotEmpty() && measurePoint.contains("|")) {
                    val points = measurePoint.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    info = if (points.size > 1 && points[0].isNotEmpty() && points[1].isNotEmpty())
                        "测点测量-测斜仪位置(${points[1]} 米)-测点序列(${points[0]})"
                    else
                        "测点测量"
                    point = if (points.size > 1) "第${points[0]}个点测量：${points[1]}米" else ""
                } else {
                    info = "测点测量"
                }
            }

            else -> {
                info = ctrMotionState.description
            }
        }

        admeMotionStatusItem.refreshMotionStatus(
            visible = true,
            isNormal = true,
            mode = mode,
            info = info,
            point = point
        )
    }

    /**
     * 显示错误模块信息对话框
     */
    private fun showErrorModulesInfoDialog() {
        if (deviceAbnormalList.isEmpty()) {
            Toaster.show("设备异常信息为空")
            return
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .asCenterList(
                "异常信息", deviceAbnormalList.toTypedArray(),
                null, -1,
                null, 0, R.layout.custom_xpopup_adapter_abnormal_info
            )
            .show()
    }
}