package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRModuleStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 遥测终端机状态信息
 *
 */
class MR702StatusInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "状态信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询设备基本信息（page=1, label=1）
        val entity1 = MRDeviceInfoEntity(pages = 1, label = 1)
        val command1 = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity1)
        commands.add(command1)

        // 查询设备模块状态信息（page=4, label=1）
        val entity2 = MRDeviceInfoEntity(pages = 4, label = 1)
        val command2 = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity2)
        commands.add(command2)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        val deviceInfo = result.data
                        if (deviceInfo.pages == "1" && deviceInfo.label == "1") {
                            initBaseInfo(deviceInfo.baseInfo)
                        } else if (deviceInfo.pages == "4" && deviceInfo.label == "1") {
                            initModuleStatusInfo(deviceInfo.moduleStatusInfo)
                        }
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    private fun initBaseInfo(baseInfo: MRBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("供电信息"))
            val externalVoltage = baseInfo.volt.toDoubleOrNull() ?: Double.MAX_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "外部电压",
                value = if (externalVoltage == 0.0) "0" else baseInfo.volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                unit = "V",
                textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                    R.color.warn_FF9D00
                ),
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initModuleStatusInfo(moduleStatusInfo: MRModuleStatusInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("模块信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "触摸屏",
                value = moduleStatusInfo.screen.compareAndReturn("1", "正常", "故障"),
                textColorRes = if (moduleStatusInfo.screen == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "4G模块",
                value = moduleStatusInfo.datanet.compareAndReturn("1", "正常", "故障"),
                textColorRes = if (moduleStatusInfo.datanet == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "有线网模块",
                value = moduleStatusInfo.wirednet.compareAndReturn("1", "正常", "故障"),
                textColorRes = if (moduleStatusInfo.wirednet == "1") 0 else ColorUtils.getColor(
                    R.color.error_FF4400
                )
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "FLASH模块",
                value = moduleStatusInfo.flash.compareAndReturn("1", "正常", "故障"),
                textColorRes = if (moduleStatusInfo.flash == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "EMMC存储模块",
                value = moduleStatusInfo.emmc.compareAndReturn("1", "正常", "故障"),
                textColorRes = if (moduleStatusInfo.emmc == "1") 0 else ColorUtils.getColor(R.color.error_FF4400),
                isBottomItem = true
            )

            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

}