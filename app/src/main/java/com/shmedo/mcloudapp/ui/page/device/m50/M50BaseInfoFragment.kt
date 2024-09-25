package com.shmedo.mcloudapp.ui.page.device.m50

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 基本信息
 */
class M50BaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("基本信息")
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<M50CurrentStateInfo>(content)
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
                val deviceStatus = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }
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
                            value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3),
                            isBottomItem = true
                        )
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "工作模式",
                    value = if (stateInfo.workMode == "1") "基站" else "测站",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "上报模式",
                    value = if (stateInfo.reportMode == "0") "常在线" else "低功耗",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "抓拍频率",
                    value = stateInfo.captureFrequency,
                    unit = "分钟/次",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "可用空间",
                    value = stateInfo.emmcFree,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "总空间",
                    value = stateInfo.emmcStorage,
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}