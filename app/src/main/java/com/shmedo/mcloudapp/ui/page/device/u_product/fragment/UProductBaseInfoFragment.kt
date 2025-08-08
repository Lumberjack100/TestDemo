package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoTextSwitcherItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/5/30
 * 描述： U产品基本信息
 * 
 * 优化特点：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的U产品特定业务逻辑不变（包括异常信息轮播功能）
 * 5. 支持4G和蓝牙两种通讯方式
 */
class UProductBaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {
    private var abnormalInfoJob: Job? = null

    private var textSwitcherItem: DeviceStatusInfoTextSwitcherItem? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    override fun queryStatusInfo() {
        abnormalInfoJob?.cancel()
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
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
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content as String)
                }
                if (commonCurrentStateInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }

                val stateInfo = commonCurrentStateInfoList[0]
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )

                val deviceAbnormalList = DeviceStatusHelper.checkDeviceAbnormal(stateInfo)
                val statusText = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                textSwitcherItem = DeviceStatusInfoTextSwitcherItem(
                    name = "设备状态",
                    value = statusText,
                    deviceStatusCode =  if (deviceAbnormalList.isEmpty()) "0" else "-3",
                    textColorRes = when (statusText) {
                        "正常" -> ColorUtils.getColor(R.color.online_colorPrimary)

                        else -> 0
                    }
                )
                groupList.add(textSwitcherItem!!)
                handleAbnormalInfo(deviceAbnormalList)

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "硬件版本",
                    value = stateInfo.hardwareVersion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件版本",
                    value = stateInfo.firmwareVersion,
                )
                stateInfo.worktime.toIntOrNull()?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "累计运行时间",
                            value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3),
                            isBottomItem = true
                        )
                    )
                }

                stateInfo.emmcStorage.notNullKey {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("存储信息"))

                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "可用空间",
                        value = stateInfo.emmcFree,
                        unit = "MB"
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "总空间",
                        value = stateInfo.emmcStorage,
                        unit = "MB",
                        isBottomItem = true
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
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