package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ul

import android.os.Bundle
import android.util.Log
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.LoraCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/8/6
 * 描述： 北斗林木生长监测终端LORA信息
 */
class ULLORAInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "LORA信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询Lora通讯参数
        val statusCommand = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_LORA_CTRL)
        commands.add(statusCommand)

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
            IOTCommandType.MD_GET_LORA_CTRL -> {
                val result = iotParseManager.parse<LoraCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_LORA_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询信息出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    private fun initParamData(info: LoraCommunicateInfo) {
        try {
            binding.refreshLayout.showContent()
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "收发频点",
                value = info.chl,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "发射功率",
                value = info.outpwr,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "空中速率",
                value = info.airbaud,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "网络编号",
                value = info.netid,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "本机地址",
                value = info.localid,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "目标地址",
                value = info.dstid,
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }
}