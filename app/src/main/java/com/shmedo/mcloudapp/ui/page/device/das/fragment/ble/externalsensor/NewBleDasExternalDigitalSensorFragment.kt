package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.ExternalDigitalSensorParamEditItem
import com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor.BaseExternalDigitalSensorFragment
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2024/6/7
 * 描述： TODO
 */
class NewBleDasExternalDigitalSensorFragment : BaseExternalDigitalSensorFragment() {
    private val mdParseManager: MDParserManager by inject()


    /**
     * 重置 静力水准/沉降仪/垂线坐标仪 初始值
     */
    override fun resetInitValue() {
        val address =
            binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                ?.findLast { it.name.contains("传感器地址") }?.value?.trim() ?: ""

        val command =
            if (iotSensorType == IOTSensorType.STATIC_LEVEL || iotSensorType == IOTSensorType.SEDIMENTATION_METER)
                MDCommandUtil.getCommand(
                    MDCommandType.SENSOR_INITIAL_READING,
                    "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                        MDCommandUtil.formatStringTwo(address)
                    }FFFFFFFF"
                )
            else
                MDCommandUtil.getCommand(
                    MDCommandType.COLLECTOR_SENSOR_REVISED,
                    "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                        MDCommandUtil.formatStringTwo(address)
                    }0,0"
                )

        commandItems.clear()
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = com.shmedo.core.commonlib.utils.AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 刷新数据
     */
    override fun refreshData() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
            "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                MDCommandUtil.formatStringTwo(sensorIndex.toString())
            }"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.SENSOR_INITIAL_READING -> {//静力水准/沉降仪 重置初始值 171
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "重置初始值出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始值已重置")
                        }
                    }
                }
            }
            MDCommandType.COLLECTOR_SENSOR_REVISED -> {//垂线坐标仪 重置初始值 165
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "重置初始值出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始值已重置")
                        }
                    }
                }
            }

            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER -> {//获取XX采集器YY通道的传感器参数 ##101
                val result = mdParseManager.parse<DasExternalSensorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "刷新传感器参数出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        initSensorInfo(result.data)
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }
}