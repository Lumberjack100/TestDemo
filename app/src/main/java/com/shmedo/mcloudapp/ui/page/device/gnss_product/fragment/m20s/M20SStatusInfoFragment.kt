package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UProductCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
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

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "limittime=60")
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.Companion.dialogConfig()
            )
        )
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<UProductCurrentStateInfo>>(content as String)
                }
                if (commonCurrentStateInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }

                binding.refreshLayout.showContent()
                val stateInfo = commonCurrentStateInfoList[0]
                val groupList = mutableListOf<Any>()

                // 供电信息
                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = stateInfo.extPowerVolt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.extPowerVolt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage > 0 && externalVoltage < 11) || externalVoltage == Double.MAX_VALUE) ColorUtils.getColor(
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
                    value = stateInfo.temp.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    textColorRes = if (internalTemp > -20 && internalTemp < 70) 0 else ColorUtils.getColor(
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
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))

                stateInfo.gnss.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "GNSS模块",
                        value = stateInfo.gnss.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.gnss.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.scl.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "倾角加速度模块",
                        value = stateInfo.scl.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.scl.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo._4g.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "4G模块",
                        value = stateInfo._4g.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo._4g.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.bt.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "蓝牙模块",
                        value = stateInfo.bt.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.bt.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.radio.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电台模块",
                        value = stateInfo.radio.uppercase().compareAndReturn("OK", "有", "无"),
                        textColorRes = if (stateInfo.radio.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.emmc.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "存储卡",
                        value = stateInfo.emmc.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.emmc.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.simCard.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "SIM卡",
                        value = stateInfo.simCard.uppercase()
                            .compareAndReturn("OK", "有", "无"),
                        textColorRes = if (stateInfo.simCard.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.flash.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "Flash模块",
                        value = stateInfo.flash.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.flash.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        ),
                        isBottomItem = true
                    )
                })

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }


}