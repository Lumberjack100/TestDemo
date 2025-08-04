package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.gw

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 自组网报警网关基本信息
 */
class GWBaseInfoFragment : BaseDeviceStatusInfoStyleFragment() {
    private val STATION_NODE_NUM = 10

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
    }

    override fun queryStatusInfo() {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_TERMINAL_ID
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询基本信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initBaseInfo(content)
                    }
                }
            }

            IOTCommandType.MD_GET_TERMINAL_ID -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_TERMINAL_ID
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询测站节点信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initTerminalIds(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBaseInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                } ?: return@launchWithViewLifecycle

                if (commonCurrentStateInfoList.isEmpty()) {
                    return@launchWithViewLifecycle
                }
                val stateInfo = commonCurrentStateInfoList[0]
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                stateInfo.sn.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "设备SN",
                        value = stateInfo.sn,
                    )
                }
                val deviceAbnormalList = DeviceStatusHelper.checkDeviceAbnormal(stateInfo)
                val deviceStatus = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "设备状态",
                        value = deviceStatus,
                        textColorRes = when (deviceStatus) {
                            "正常" -> ColorUtils.getColor(R.color.online_colorPrimary)
                            "告警" -> ColorUtils.getColor(
                                R.color.warn_FF9D00
                            )

                            else -> ColorUtils.getColor(R.color.error_FF4400)
                        }
                    )
                )
                stateInfo.hardwareVersion.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "硬件版本",
                        value = stateInfo.hardwareVersion,
                    )
                }
                stateInfo.firmwareVersion.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "固件版本",
                        value = stateInfo.firmwareVersion,
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

    private fun initTerminalIds(content: String) {
        if (content.isEmpty())
            return

        launchWithViewLifecycle {
            try {
                val groupList = mutableListOf<Any>()
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("测站节点信息"))

                //用逗号分割
                content.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .forEachIndexed { index, terminalId ->
                        if (index < STATION_NODE_NUM) {
                            groupList.add(DeviceStatusInfoBasicItem("测站${index + 1}编号", terminalId))
                        }
                    }

                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }
}