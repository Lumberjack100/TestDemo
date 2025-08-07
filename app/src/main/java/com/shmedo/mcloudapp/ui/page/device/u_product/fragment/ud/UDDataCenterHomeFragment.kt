package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/19
 * @desc: 一体式雷达水位/泥位计数据中心列表页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class UDDataCenterHomeFragment : BaseDataCenterHomeFragment() {
    override fun getNavigationActionId(): Int {
        return R.id.action_global_to_dataCenterParamFragment
    }

    override fun queryData() {
        val commands = mutableListOf<String>()
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=1"))

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initDataCenterStatus(result.data)
                    }
                }
            }

            else -> {
            }
        }
    }

    private fun initDataCenterStatus(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                if (stateInfo.dataCenterEnableStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterLinkStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterEnableStatus.isNotEmpty()
                    && stateInfo.dataCenterLinkStatus.isNotEmpty()
                ) {
                    //根据逗号分隔
                    val enableStatusList = stateInfo.dataCenterEnableStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    val onlineStatusList = stateInfo.dataCenterLinkStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    val lastIndex = enableStatusList.size.coerceAtMost(centerNum)
                    for (i in 0 until lastIndex) {
                        val status =
                            if (enableStatusList[i] == "0") "0" else if (onlineStatusList[i] == "1") "1" else "2"

                        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                            ?.findLast { it.name.contains("数据链路${i + 1}") }
                            ?.refreshStatus(status)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}