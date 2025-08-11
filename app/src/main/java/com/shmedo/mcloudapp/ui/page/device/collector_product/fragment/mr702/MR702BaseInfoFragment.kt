package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRunningData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 遥测终端机基本信息
 *
 */
class MR702BaseInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询设备基本信息（page=1, label=1）
        val entity1 = MRDeviceInfoEntity(pages = 1, label = 1)
        val command1 = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity1)
        commands.add(command1)

        // 查询设备运行数据（page=2, label=2）
        val entity2 = MRDeviceInfoEntity(pages = 2, label = 2)
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
                        } else if (deviceInfo.pages == "2" && deviceInfo.label == "2") {
                            initRunningData(deviceInfo.runningData)
                        }
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    private fun initBaseInfo(stateInfo: MRBaseInfo) {
        launchWithViewLifecycle {
            try {
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "产品型号",
                    value = deviceInfo.deviceName,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = deviceInfo.deviceToken,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件版本",
                    value = stateInfo.ver,
                )
                deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                    addDeviceLogItem(Log.ERROR, error.errorMsg)
                }?.let { deviceDetailInfo ->
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "注册时间",
                        value = deviceDetailInfo.deviceInfo.createTime,
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initRunningData(runningData: MRRunningData) {
        try {
            val groupList = mutableListOf<Any>()

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "重启次数",
                value = runningData.rebootn,
            )
            runningData.otime.toIntOrNull()?.let {
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "本次运行时间",
                        value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3)
                    )
                )
            }
            runningData.ttime.toIntOrNull()?.let {
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "累计运行时间",
                        value = DeviceStatusInfoProcessor.millis2FitTimeSpan(it * 1000L, 3),
                        isBottomItem = true
                    )
                )
            }

            if (runningData.ustorage.isNotEmpty() && runningData.tstorage.isNotEmpty()) {
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("存储信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "可用空间",
                    value = DeviceStatusInfoProcessor.formatDoubleValue(
                        (runningData.tstorage.toDouble() - runningData.ustorage.toDouble()).toString(),
                        "0",
                        2
                    ),
                    unit = "GB"
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "总空间",
                    value = runningData.tstorage,
                    unit = "GB",
                    isBottomItem = true
                )
            }

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