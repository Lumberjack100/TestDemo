package com.shmedo.mcloudapp.ui.page.device.m50

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

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外接电压",
                    value = stateInfo.externalVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "光伏板电压",
                    value = stateInfo.solarVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "V",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("电池信息"))
                val batteryInfoList = stateInfo.battery ?: emptyList()
                batteryInfoList.onEachIndexed { index, batteryInfo ->
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}电压",
                        value = batteryInfo.batteryStatus.compareAndReturn(
                            "-3",
                            AppContants.PLACE_HOLDER_VALUE,
                            batteryInfo.batteryVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE }),
                        unit = "V",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}电量",
                        value = batteryInfo.batteryStatus.compareAndReturn(
                            "-3",
                            AppContants.PLACE_HOLDER_VALUE,
                            batteryInfo.batteryCapacity.ifEmpty { AppContants.PLACE_HOLDER_VALUE }),
                        unit = "%",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}状态",
                        value = if (batteryInfo.batteryStatus == "1") "充电中" else if (batteryInfo.batteryStatus == "0") "放电中" else AppContants.PLACE_HOLDER_VALUE,
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}温度",
                        value = batteryInfo.batteryStatus.compareAndReturn(
                            "-3",
                            AppContants.PLACE_HOLDER_VALUE,
                            batteryInfo.batteryTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE }),
                        unit = "℃",
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池${index + 1}健康度",
                        value = batteryInfo.batteryStatus.compareAndReturn(
                            "-3",
                            AppContants.PLACE_HOLDER_VALUE,
                            batteryInfo.batteryHealth.ifEmpty { AppContants.PLACE_HOLDER_VALUE }),
                        unit = "%",
                        isBottomItem = true
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.internalTemp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.internalHumidity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "GNSS模组",
                    value = stateInfo.gnss.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.gnss.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "加速度计",
                    value = stateInfo.scl.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.scl.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "4G模组",
                    value = stateInfo._4g.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo._4g.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙模组",
                    value = stateInfo.bt.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.bt.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "Lora模组",
                    value = stateInfo.lora.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.lora.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "存储卡",
                    value = stateInfo.sd.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.sd.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "温湿度模组",
                    value = stateInfo.sht21.uppercase().compareAndReturn("OK", "正常", "异常"),
                    textColorRes = if (stateInfo.sht21.uppercase() == "OK") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else R.color.error_FF4400,
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