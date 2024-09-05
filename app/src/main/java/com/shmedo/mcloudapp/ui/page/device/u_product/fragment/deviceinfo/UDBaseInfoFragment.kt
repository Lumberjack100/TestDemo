package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.UDCommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： 一体化雷达泥位计基本信息
 */
class UDBaseInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "value=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCommonCurrentStateInfo>(content)
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
                val deviceStatus = if (stateInfo.deviceStatus == "0") "正常" else "异常"
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "设备状态",
                        value = deviceStatus,
                        textColorRes = if (deviceStatus == "正常") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                            R.color.red_F13838
                        )
                    )
                )
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
                            value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3)
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "上报模式",
                    value = if (stateInfo.reportMode == "0") "自动" else "手动",
                )
                val reportStatus = when (stateInfo.reportStatus) {
                    "1" -> "一级报警"
                    "2" -> "二级报警"
                    "3" -> "三级报警"
                    "4" -> "四级报警"
                    "5" -> "正常"
                    else -> "正常"
                }
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "上报状态",
                        value = reportStatus,
                        textColorRes = if (reportStatus == "正常") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                            R.color.red_F13838
                        )
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "上报频率",
                    value = stateInfo.reportFrequency,
                    unit = "分钟/次",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "抓拍频率",
                    value = stateInfo.captureFrequency,
                    unit = "分钟/次",
                )
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = UDBaseInfoFragment()
    }
}