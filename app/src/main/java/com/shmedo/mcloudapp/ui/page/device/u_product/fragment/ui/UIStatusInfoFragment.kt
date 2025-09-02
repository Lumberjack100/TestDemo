package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ui

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
 * 描述： : 一体式倾斜仪状态信息
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的U产品特定业务逻辑不变
 * 5. 支持4G和蓝牙两种通讯方式
 */
class UIStatusInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
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
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }

                val stateInfo = commonCurrentStateInfoList[0]
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = stateInfo.extPowerVolt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else stateInfo.extPowerVolt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("电池信息"))
                val batteryVoltage =
                    stateInfo.batPowerVolt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电池电压",
                    value = if (batteryVoltage == 0.0) "0" else stateInfo.batPowerVolt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = stateInfo.temp.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.temp.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
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
                stateInfo.lora.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "LORA模块",
                        value = stateInfo.lora.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.lora.uppercase() == "OK") 0 else ColorUtils.getColor(
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
                stateInfo.ld.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "雷达模块",
                        value = stateInfo.ld.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.ld.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.radio.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电台模块",
                        value = stateInfo.radio.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.radio.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.cam.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "摄像头模块",
                        value = stateInfo.cam.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.cam.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
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
                stateInfo.adc.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电压采集模块",
                        value = stateInfo.adc.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.adc.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.emmc.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "存储器模块",
                        value = stateInfo.emmc.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.emmc.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.sht21.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "温湿度模块",
                        value = stateInfo.sht21.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.sht21.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.qmc5883.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "磁力计模块",
                        value = stateInfo.qmc5883.uppercase()
                            .compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.qmc5883.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.battery.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "电池模块",
                        value = stateInfo.battery.uppercase()
                            .compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.battery.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.simCard.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "SIM卡",
                        value = stateInfo.simCard.uppercase()
                            .compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.simCard.uppercase() == "OK") 0 else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                })
                stateInfo.rtc.notNullKey(notNullKeyAction = {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "系统时钟",
                        value = stateInfo.rtc.uppercase().compareAndReturn("OK", "正常", "故障"),
                        textColorRes = if (stateInfo.rtc.uppercase() == "OK") 0 else ColorUtils.getColor(
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