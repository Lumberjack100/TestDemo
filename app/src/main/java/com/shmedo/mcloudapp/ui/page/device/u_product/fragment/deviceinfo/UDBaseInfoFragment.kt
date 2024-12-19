package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNull
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoTextSwitcherItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： 一体化雷达泥位计基本信息
 */
class UDBaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private var abnormalInfoJob: Job? = null

    private var textSwitcherItem: DeviceStatusInfoTextSwitcherItem? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("基本信息")
    }

    override fun queryStatusInfo() {
        abnormalInfoJob?.cancel()
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备型号",
                    value = stateInfo.deviceType,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )
                val statusText = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }
                textSwitcherItem = DeviceStatusInfoTextSwitcherItem(
                    name = "设备状态",
                    value = statusText,
                    deviceStatusCode = stateInfo.deviceStatus,
                    textColorRes = when (statusText) {
                        "正常" -> ColorUtils.getColor(R.color.online_colorPrimary)

                        else -> 0
                    }
                )
                groupList.add(textSwitcherItem!!)
                processAbnormalInfo(stateInfo.deviceError, stateInfo.deviceWarn)

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件版本",
                    value = stateInfo.firmwareVersion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件日期",
                    value = stateInfo.firmwareDate,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "启动代码",
                    value = stateInfo.bootCode,
                    //代码以 36 开头或者是 2026、2027、2028 的显示告警色
                    textColorRes = if (stateInfo.bootCode.startsWith("36") || stateInfo.bootCode in listOf(
                            "2026",
                            "2027",
                            "2028"
                        )
                    ) ColorUtils.getColor(R.color.warn_FF9D00) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "启动时间",
                    value = stateInfo.bootTime,
                )
                stateInfo.runTime.toIntOrNull()?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "本次运行时间",
                            value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3)
                        )
                    )
                }
                stateInfo.totalRunTime.toIntOrNull()?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "累计运行时间",
                            value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3),
                            isBottomItem = true
                        )
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "上报模式",
                    value = if (stateInfo.reportMode == "0") "自动" else "手动",
                )
                val reportStatus = when (stateInfo.reportStatus) {
                    "0" -> "加报"
                    "1" -> "一级报警"
                    "2" -> "二级报警"
                    "3" -> "三级报警"
                    "4" -> "四级报警"
                    "5" -> "普通"
                    else -> "普通"
                }
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "上报状态",
                        value = reportStatus,
                        textColorRes = if (reportStatus == "普通") ColorUtils.getColor(R.color.online_colorPrimary) else ColorUtils.getColor(
                            R.color.warn_FF9D00
                        )
                    )
                )

                var frequency = stateInfo.reportFrequency.toIntOrNull()?.let { it / 60 } ?: 0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "上报频率",
                    value = if (frequency <= 0) stateInfo.reportFrequency else frequency.toString(),
                    unit = if (frequency <= 0) "分钟/次" else "小时/次",
                )

                frequency = stateInfo.captureFrequency.toIntOrNull()?.let { it / 60 } ?: 0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "抓拍频率",
                    value = if (frequency <= 0) stateInfo.captureFrequency else frequency.toString(),
                    unit = if (frequency <= 0) "分钟/次" else "小时/次",
                    isBottomItem = true
                )
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun processAbnormalInfo(
        deviceError: Map<String, String>? = null,
        deviceWarn: Map<String, String>? = null
    ) {
        val errorInfoList = mutableListOf<String>()
        try {
            deviceError.notNull(notNullAction = { resultMap ->
                resultMap["ld"]?.let { errorInfoList.add("雷达故障") }
                resultMap["cam"]?.let { errorInfoList.add("摄像头故障") }
                resultMap["qj"]?.let { errorInfoList.add("加速度计故障") }
                resultMap["4G"]?.let { errorInfoList.add("4G故障") }
                resultMap["bt"]?.let { errorInfoList.add("蓝牙故障") }
                resultMap["radio"]?.let { errorInfoList.add("电台故障") }
                resultMap["flash"]?.let { errorInfoList.add("存储故障") }
                resultMap["ath"]?.let { errorInfoList.add("温湿度故障") }
            })
            deviceWarn.notNull(notNullAction = { resultMap ->
                resultMap["loc_offset"]?.let { errorInfoList.add("位置偏移") }
                resultMap["angle_offset"]?.let { errorInfoList.add("角度偏移") }
                resultMap["extern_volt"]?.let { volt -> errorInfoList.add(if (volt == "-1") "外部电压过高" else "外部电压过低") }
                resultMap["bat_cap"]?.let { errorInfoList.add("电池电量过低") }
                resultMap["bat_temp"]?.let { errorInfoList.add("电池温度过高") }
                resultMap["bat_health"]?.let { errorInfoList.add("电池容量过低") }
                resultMap["inside_temp"]?.let { temp -> errorInfoList.add(if (temp == "-1") "内部温度过高" else "内部温度过低") }
                resultMap["sim_card"]?.let { errorInfoList.add("无SIM卡") }
            })
            handleAbnormalInfo(errorInfoList)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理设备异常信息轮播展示
     * 每隔3秒切换一次，取出异常信息列表中的每一条异常信息，轮播显示
     */
    private fun handleAbnormalInfo(errorInfoList: List<String>) {
        // 取消之前的job（如果存在）
        abnormalInfoJob?.cancel()

        //如果列表为空，直接返回
        if (errorInfoList.isEmpty()) {
            return
        }
        if (errorInfoList.size == 1) {
            textSwitcherItem?.refreshValue(value = errorInfoList[0])
            return
        }
        abnormalInfoJob = launchWithViewLifecycle {
            flow {
                while (true) {
                    errorInfoList.forEach { errorInfo ->
                        emit(errorInfo)
                        delay(1500) // 延迟3秒
                    }
                }
            }.collect { errorInfo ->
                textSwitcherItem?.refreshValue(value = errorInfo)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}