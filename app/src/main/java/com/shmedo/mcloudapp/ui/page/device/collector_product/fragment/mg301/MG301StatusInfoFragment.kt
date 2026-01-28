package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mg301

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mg301.MG301DeviceStatusInfo
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
 * 创建时间：2026/01/19
 * 描述：多模融合通讯数据网关(MG301)状态信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用统一的优化架构
 * 2. 使用 MD_GET_DEVICE_STATUS 指令获取设备状态信息
 * 3. 显示三个信息分组：供电信息、环境信息、模块信息
 *
 * 页面布局：
 * - 供电信息：外部电压
 * - 环境信息：内部温度、内部湿度
 * - 模块信息：4G模块、LORA模块、卫通模块、蓝牙模块、温湿度模块、RTC模块、SIM卡、EMMC模块
 */
class MG301StatusInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    /**
     * 初始化页面视图
     * 设置 Toolbar 标题
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    /**
     * 查询设备状态信息
     * 发送 MD_GET_DEVICE_STATUS 指令获取设备当前状态
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 构建获取设备状态的指令
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "limittime=60")
        commands.add(command)

        // 发送指令序列
        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载弹窗
                errorConfig = ErrorConfig.Companion.dialogConfig()
            )
        )
    }

    /**
     * 初始化状态信息显示
     * 解析设备返回的状态数据并构建 UI 列表
     *
     * @param content 设备返回的 JSON 字符串数据
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<MG301DeviceStatusInfo>(content as String)
                }

                // 数据校验：确保状态信息不为空
                if (statusInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }

                // 显示内容区域
                binding.refreshLayout.showContent()

                // 构建 UI 列表
                val groupList = mutableListOf<Any>()

                // 添加供电信息分组
                addPowerInfoGroup(groupList, statusInfo)

                // 添加环境信息分组
                addEnvironmentInfoGroup(groupList, statusInfo)

                // 添加模块信息分组
                addModuleInfoGroup(groupList, statusInfo)

                // 设置 RecyclerView 数据
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 添加供电信息分组
     * 显示外部电压信息，电压低于 11V 时显示警告色
     *
     * @param groupList 列表数据容器
     * @param statusInfo 设备状态信息
     */
    private fun addPowerInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: MG301DeviceStatusInfo
    ) {
        // 添加分组标题
        groupList.add(DeviceStatusInfoGroupItem("供电信息"))

        // 解析外部电压值
        val externalVoltage = statusInfo.extPowerVolt.toDoubleOrNull() ?: Double.MAX_VALUE

        // 添加外部电压项
        // 电压为 0 时显示 "0"，否则显示实际值；电压异常（0-11V 或无效）时显示警告色
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "外部电压",
            value = if (externalVoltage == 0.0) "0" else statusInfo.extPowerVolt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
            unit = "V",
            textColorRes = if ((externalVoltage > 0 && externalVoltage < 11) || externalVoltage == Double.MAX_VALUE) {
                ColorUtils.getColor(R.color.warn_FF9D00)
            } else 0,
            isBottomItem = true
        )
    }

    /**
     * 添加环境信息分组
     * 显示内部温度和湿度信息，温度超出正常范围时显示警告色
     *
     * @param groupList 列表数据容器
     * @param statusInfo 设备状态信息
     */
    private fun addEnvironmentInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: MG301DeviceStatusInfo
    ) {
        // 添加分组间隔和标题
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("环境信息"))

        // 添加内部温度项
        // 温度正常范围：-20℃ ~ 70℃，超出范围显示警告色
        val internalTemp = statusInfo.temp
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "内部温度",
            value = statusInfo.temp.toString(),
            unit = "℃",
            textColorRes = if (internalTemp > -20 && internalTemp < 70) 0 else {
                ColorUtils.getColor(R.color.warn_FF9D00)
            }
        )

        // 添加内部湿度项
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "内部湿度",
            value = statusInfo.humidity.toString(),
            unit = "%",
            isBottomItem = true
        )
    }

    /**
     * 添加模块信息分组
     * 显示各硬件模块的工作状态（正常/故障/有/无）
     *
     * @param groupList 列表数据容器
     * @param statusInfo 设备状态信息
     */
    private fun addModuleInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: MG301DeviceStatusInfo
    ) {
        // 添加分组间隔和标题
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("模块信息"))

        // 4G 模块状态
        statusInfo._4g.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "4G模块",
                value = statusInfo._4g.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo._4g)
            )
        })

        // LoRa 模块状态
        statusInfo.lora.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "LORA模块",
                value = statusInfo.lora.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.lora)
            )
        })

        // 卫通模块状态
        statusInfo.satcom.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "卫通模块",
                value = statusInfo.satcom.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.satcom)
            )
        })

        // 蓝牙模块状态
        statusInfo.bt.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "蓝牙模块",
                value = statusInfo.bt.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.bt)
            )
        })

        // 温湿度模块状态 (SHT21 传感器)
        statusInfo.sht21.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "温湿度模块",
                value = statusInfo.sht21.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.sht21)
            )
        })

        // RTC 模块状态
        statusInfo.rtc.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "RTC模块",
                value = statusInfo.rtc.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.rtc)
            )
        })

        // SIM 卡状态（有/无）
        statusInfo.simCard.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "SIM卡",
                value = statusInfo.simCard.uppercase().compareAndReturn("OK", "有", "无"),
                textColorRes = getModuleStatusColor(statusInfo.simCard)
            )
        })

        // EMMC 存储模块状态
        statusInfo.emmc.notNullKey(notNullKeyAction = {
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "EMMC模块",
                value = statusInfo.emmc.uppercase().compareAndReturn("OK", "正常", "故障"),
                textColorRes = getModuleStatusColor(statusInfo.emmc),
                isBottomItem = true
            )
        })
    }

    /**
     * 获取模块状态对应的文字颜色
     * OK 状态显示默认颜色，其他状态显示错误色
     *
     * @param status 模块状态字符串
     * @return 颜色值，0 表示使用默认颜色
     */
    private fun getModuleStatusColor(status: String): Int {
        return if (status.uppercase() == "OK") 0 else ColorUtils.getColor(R.color.error_FF4400)
    }
}
