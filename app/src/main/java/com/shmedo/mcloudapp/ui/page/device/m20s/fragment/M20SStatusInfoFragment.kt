package com.shmedo.mcloudapp.ui.page.device.m20s.fragment

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
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
 * 描述： 状态信息
 */
class M20SStatusInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("状态信息")
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))

                val externalVoltage = stateInfo.ext_power_volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.ext_power_volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = stateInfo.temp.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.temp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if ((internalTemp > -20 && internalTemp < 70) || internalTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "℃",
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.humidity.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "%",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))
                if (stateInfo.self_check.uppercase().indexOf("GNSS") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "GNSS模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("GNSS:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("GNSS:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("SCL") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "加速度计",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("SCL:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("SCL:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("4G") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "4G模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("4G:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("4G:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("BT") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "蓝牙模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("BT:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("BT:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("RADIO") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电台模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("RADIO:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("RADIO:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("EMMC") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "存储模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("EMMC:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("EMMC:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("SHT21") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "温湿度模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("SHT21:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("SHT21:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("MEMS") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "倾角计模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("MEMS:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("MEMS:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                }
                if (stateInfo.self_check.uppercase().indexOf("SOLAR485") != -1) {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "太阳能控制器模块",
                        value = if (stateInfo.self_check.uppercase()
                                .indexOf("SOLAR485:0") == -1
                        ) "正常" else "异常",
                        textColorRes = if (stateInfo.self_check.uppercase()
                                .indexOf("SOLAR485:0") == -1
                        ) 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        ),
                        isBottomItem = true
                    )
                }

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}