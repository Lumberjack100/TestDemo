package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
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
class M50StatusInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("状态信息")
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<M50CurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "光伏板电压",
                    value = stateInfo.solarVoltage.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V"
                )

                val externalVoltage = stateInfo.externalVoltage.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.externalVoltage.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("电池信息"))
                val batteryInfoList = stateInfo.battery ?: emptyList()
                batteryInfoList.onEachIndexed { index, batteryInfo ->
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}状态",
                        value = when (batteryInfo.batteryStatus) {
                            "0" -> "放电中"
                            "1" -> "充电中"
                            "2" -> "空闲"
                            "-1" -> "异常"
                            else -> AppContants.PLACE_HOLDER_VALUE
                        },
                        textColorRes = if (batteryInfo.batteryStatus == "-1") ColorUtils.getColor(
                            R.color.error_FF4400
                        ) else 0,
                    )

                    val batteryVoltage =
                        batteryInfo.batteryVoltage.toDoubleOrNull() ?: Double.MAX_VALUE
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}电压",
                        value = if (batteryVoltage == 0.0) "0" else batteryInfo.batteryVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        unit = "V",
                    )

                    val batteryCapacity =
                        batteryInfo.batteryCapacity.toDoubleOrNull() ?: Double.MAX_VALUE
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}电量",
                        value = batteryInfo.batteryCapacity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        unit = "%",
                        textColorRes = if (batteryCapacity > 25) 0 else ColorUtils.getColor(
                            R.color.warn_FF9D00
                        ),
                    )

                    val batteryTemp = batteryInfo.batteryTemp.toDoubleOrNull() ?: Double.MAX_VALUE
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}温度",
                        value = batteryInfo.batteryTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        textColorRes = if ((batteryTemp > -20 && batteryTemp < 80) || batteryTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                            R.color.warn_FF9D00
                        ),
                        unit = "℃",
                    )

                    val batteryHealth =
                        batteryInfo.batteryHealth.toDoubleOrNull() ?: Double.MAX_VALUE
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}最大容量",
                        value = batteryInfo.batteryHealth.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        textColorRes = if (batteryHealth > 70) 0 else ColorUtils.getColor(
                            R.color.warn_FF9D00
                        ),
                        unit = "%",
                        isBottomItem = index == batteryInfoList.size - 1
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = stateInfo.internalTemp.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.internalTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if ((internalTemp > -20 && internalTemp < 70) || internalTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "℃",
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.internalHumidity.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "GNSS模块",
                    value = stateInfo.gnss.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.gnss.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "加速度计",
                    value = stateInfo.scl.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.scl.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "4G模块",
                    value = stateInfo._4g.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo._4g.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "SIM卡",
                    value = stateInfo.simStatus.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.simStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙模块",
                    value = stateInfo.bt.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.bt.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台模块",
                    value = stateInfo.lora.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.lora.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "存储卡",
                    value = stateInfo.sd.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.sd.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    )
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "温湿度模块",
                    value = stateInfo.sht21.uppercase().compareAndReturn("OK", "正常", "故障"),
                    textColorRes = if (stateInfo.sht21.uppercase() == "OK") 0 else ColorUtils.getColor(
                        R.color.error_FF4400
                    ),
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}