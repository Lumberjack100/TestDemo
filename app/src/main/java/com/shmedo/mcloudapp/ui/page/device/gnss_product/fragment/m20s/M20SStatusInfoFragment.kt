package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

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
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述：普适型 GNSS 接收机(M20S)状态信息
 *
 * 优化特点：
 * 1. 继承自OptimizedBaseDeviceStatusInfoStyle2Fragment，使用新的优化架构
 * 2. 统一的错误处理和指令执行机制
 * 3. 保持原有的M20S状态信息显示逻辑不变
 * 4. 支持供电信息、环境信息、模块信息等状态监控
 */
class M20SStatusInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // 供电信息
                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = stateInfo.ext_power_volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.ext_power_volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if (externalVoltage > 0 && externalVoltage < 11) ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ) else 0,
                    isBottomItem = true
                )

                // 环境信息
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

                // 模块信息
                stateInfo.self_check.notNullKey {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("模块信息"))

                    // GNSS模块
                    if (stateInfo.self_check.uppercase().indexOf("GNSS") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "GNSS模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("GNSS:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("GNSS:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 倾角加速度模块
                    if (stateInfo.self_check.uppercase().indexOf("MEMS") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "倾角加速度模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("MEMS:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("MEMS:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 倾角加速度模块
                    if (stateInfo.self_check.uppercase().indexOf("SCL") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "倾角加速度模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("SCL:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("SCL:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 4G模块
                    if (stateInfo.self_check.uppercase().indexOf("4G") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "4G模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("4G:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("4G:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 蓝牙模块
                    if (stateInfo.self_check.uppercase().indexOf("BT") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "蓝牙模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("BT:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("BT:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 电台模块
                    if (stateInfo.self_check.uppercase().indexOf("RADIO") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "电台模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("RADIO:0") == -1
                            ) "有" else "无",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("RADIO:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 存储模块
                    if (stateInfo.self_check.uppercase().indexOf("EMMC") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "存储卡",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("EMMC:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("EMMC:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    }

                    // 温湿度模块
                    if (stateInfo.self_check.uppercase().indexOf("SHT21") != -1) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "温湿度模块",
                            value = if (stateInfo.self_check.uppercase()
                                    .indexOf("SHT21:0") == -1
                            ) "正常" else "故障",
                            textColorRes = if (stateInfo.self_check.uppercase()
                                    .indexOf("SHT21:0") == -1
                            ) 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            ),
                            isBottomItem = true
                        )
                    }
                }

                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }
}