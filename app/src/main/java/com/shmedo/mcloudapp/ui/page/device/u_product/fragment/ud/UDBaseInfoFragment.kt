package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

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
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoTextSwitcherItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import com.shmedo.mcloudapp.utils.UDDeviceStatusHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： 一体式雷达水位/泥位计基本信息
 */
class UDBaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {
    private var abnormalInfoJob: Job? = null

    private var textSwitcherItem: DeviceStatusInfoTextSwitcherItem? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }


    override fun queryStatusInfo() {
        abnormalInfoJob?.cancel()
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=0")
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content as String)
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
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun processAbnormalInfo(
        deviceError: Map<String, String>? = null,
        deviceWarn: Map<String, String>? = null
    ) {
        try {
            val errorInfoList = UDDeviceStatusHelper.processAbnormalInfo(deviceError, deviceWarn)
            handleAbnormalInfo(errorInfoList)
        } catch (e: Exception) {
            Timber.Forest.e(e)
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

}