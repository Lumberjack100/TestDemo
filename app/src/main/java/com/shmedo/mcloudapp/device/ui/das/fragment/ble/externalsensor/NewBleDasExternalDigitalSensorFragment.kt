package com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.ExternalDigitalSensorParamEditItem
import com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor.BaseExternalDigitalSensorFragment
import com.shmedo.mcloudapp.ext.showLoadingDialog
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
                MDCommandUtil.getCommand(MDCommandType.SENSOR_INITIAL_READING, "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${MDCommandUtil.formatStringTwo(address)}FFFFFFFF")
            else
                MDCommandUtil.getCommand(MDCommandType.COLLECTOR_SENSOR_REVISED, "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${MDCommandUtil.formatStringTwo(address)}0,0")

        commandItems.clear()
        commandItems.add(command)
        refreshData()

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 重置静力水准初始值后 刷新数据
     */
    private fun refreshData() {
        //查询静力水准配置信息
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
            "${MDCommandUtil.formatStringTwo(iotSensorType.code)}${
                MDCommandUtil.formatStringTwo(sensorIndex.toString())
            }"
        )
        commandItems.add(command)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.SENSOR_INITIAL_READING -> {//设置量水堰初始读数 171
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "量水堰初始读数设置出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
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
                        initSensorInfo(result.data)
                        sendCommandFromCmdList()
                    }
                }
            }

            else -> {

            }
        }
    }
}