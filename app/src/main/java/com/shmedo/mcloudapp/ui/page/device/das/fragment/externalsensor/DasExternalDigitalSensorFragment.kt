package com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasExternalSensorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.model.ExternalDigitalSensorParamEditItem
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2024/6/7
 * 描述： TODO
 */
class DasExternalDigitalSensorFragment : BaseExternalDigitalSensorFragment() {
    private val iotParseManager: IOTParserManager by inject()

    /**
     * 重置 静力水准/沉降仪/垂线坐标仪 初始值
     */
    override fun resetInitValue() {
        val address =
            binding.recyclerview.models?.filterIsInstance<ExternalDigitalSensorParamEditItem>()
                ?.findLast { it.name.contains("传感器地址") }?.value?.trim() ?: ""

        val entity = DasExternalSensorEntity().apply {
            index = sensorIndex.toString()
            type = iotSensorType.code
            addr = address
            initval =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) IOTConstants.NULL_KEY else "FFFFFFFF"
            initvalx =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) "0" else IOTConstants.NULL_KEY
            initvaly =
                if (iotSensorType == IOTSensorType.VERTICAL_COORDINATE) "0" else IOTConstants.NULL_KEY
        }
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
            entity.toCommandString()
        )

        commandItems.clear()
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_10000_MILLIS
        )
    }

    /**
     * 刷新数据
     */
    override fun refreshData() {
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(
                IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR,
                "index=$sensorIndex"
            )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "重置初始值出错: ${result.message}"
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

            IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR -> {
                val result = iotParseManager.parse<DasExternalSensorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "刷新传感器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
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