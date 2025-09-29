package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasCollectorEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasExternalSensorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import org.koin.android.ext.android.inject

/**
 * 创建者：gonghe
 * 创建时间：2024/6/7
 * 描述：物联网采集器(DAS)扩展传感器列表页面 - 支持4G通讯方式
 */
class DasSensorListFragment : BaseDasSensorListFragment() {
    private val iotParseManager: IOTParserManager by inject()

    /**
     * 查询采集器配置信息
     */
    override fun queryCollectorInfo() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL)
        executeCollectorInfoCommands(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    override fun queryExtendSensorConfigInfo(sensorNum: Int) {
        val commands = buildList {
            for (i in 0 until sensorNum) {
                add(
                    IOTCommandUtil.getCommand(
                        IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR,
                        "index=$i"
                    )
                )
            }
        }

        if (commands.isEmpty()) {
            finishRefresh()
            return
        }

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            ),
            callbacks = CommandSequenceCallbacks(
                onComplete = {
                    updateFooter()
                }
            )
        )
    }

    /**
     * 当接入的传感器个数为0时，设置采集器地址为0，关闭采集器
     */
    override fun closeCollector() {
        val entity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            addr = "0",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            entity.toCommandString()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            ),
            callbacks = CommandSequenceCallbacks(
                onComplete = {
                    showMessageDialog(StringUtils.getString(R.string.collector_closed_warn))
                }
            )
        )
    }

    /**
     * 删除传感器
     */
    override fun onDeleteSensor() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR,
            "index=$deleteItemIndex"
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun initSaveCommand() {
        val isWeatherStation =
            IOTSensorType.value(mStates.collectorType.get()) == IOTSensorType.WEATHER_STATION

        val commands = mutableListOf<String>()
        if (isWeatherStation) {
            //多要素气象计选择传感器厂家
            commands += IOTCommandUtil.getCommand(
                IOTCommandType.MD_RAW,
                "content=##1404"
            )
        }

        //设置采集器参数
        val collectorEntity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            sensornum = mStates.sensorModelMap.size.toString(),
        )
        commands += IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            collectorEntity.toCommandString()
        )

        //设置采集器接入的传感器配置信息
        commands += buildExtendSensorConfigCommands()

        if (isWeatherStation) {
            commands += IOTCommandUtil.getCommand(
                IOTCommandType.MD_RAW,
                "content=##0192"
            )
            commands += IOTCommandUtil.getCommand(
                IOTCommandType.MD_RAW,
                "content=##0081"
            )
        }

        if (commands.isEmpty()) {
            return
        }

        val completionAction: () -> Unit = when {
            isWeatherStation -> {
                {
                    showMessage(
                        StringUtils.getString(R.string.device_reboot_tip),
                        "温馨提示",
                        "确定"
                    ) {
                        nav().navigateUp()
                    }
                }
            }

            else -> {
                {
                    Toaster.show("保存成功")
                }
            }
        }

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            ),
            callbacks = CommandSequenceCallbacks(
                onComplete = {
                    completionAction()
                }
            )
        )
    }

    /**
     * 设置采集器接入的传感器配置信息
     */
    private fun buildExtendSensorConfigCommands(): List<String> {
        val commands = mutableListOf<String>()
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEachIndexed { index, key ->
                val sensorInfo = mStates.sensorModelMap[key]!!
                val entity = DasExternalSensorEntity().apply {
                    this.index = index.toString()
                    type = sensorInfo.type
                    addr = sensorInfo.addr
                    threshold = sensorInfo.threshold
                    corrval = sensorInfo.corrval

                    when (IOTSensorType.value(sensorInfo.type)) {
                        IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                            poly_a = sensorInfo.poly_a
                            poly_b = sensorInfo.poly_b
                            poly_c = sensorInfo.poly_c
                            temp_k = sensorInfo.temp_k
                            temp_t0 = sensorInfo.temp_t0
                            ropelen = sensorInfo.ropelen
                            tubealti = sensorInfo.tubealti
                        }

                        IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            referval_f = sensorInfo.referval_f
                            temp_t0 = sensorInfo.temp_t0
                            ropelen = sensorInfo.ropelen
                            tubealti = sensorInfo.tubealti
                        }

                        IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            referval_f = sensorInfo.referval_f
                            temp_t0 = sensorInfo.temp_t0
                        }

                        IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            referval_f = sensorInfo.referval_f
                            temp_t0 = sensorInfo.temp_t0
                            elastic_mod = sensorInfo.elastic_mod
                        }

                        IOTSensorType.VIBRATING_SENSOR -> {//MCU_振弦传感器
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            referval_f = sensorInfo.referval_f
                            temp_t0 = sensorInfo.temp_t0
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.WEIR -> {//量水堰计
                            lsycsds = sensorInfo.lsycsds
                            lsyysst = sensorInfo.lsyysst
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.WATER_LEVEL_GAUGE -> {//MCU_水位(液位)计
                            ropelen = sensorInfo.ropelen
                            tubealti = sensorInfo.tubealti
                        }

                        /** 以下是数字式传感器   **/
                        IOTSensorType.INCLINOMETER -> {//固定测斜仪
                            spacing = sensorInfo.spacing
                            model_type = sensorInfo.model_type
                        }

                        IOTSensorType.RADAR_LEVEL_GAUGE -> {//雷达液(物)位计 设置子雷达传感器型号
                            child_type = sensorInfo.child_type
                        }

                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
                            initvalx = sensorInfo.initvalx
                            initvaly = sensorInfo.initvaly
                            initvalz = sensorInfo.initvalz
                        }

                        IOTSensorType.STATIC_LEVEL,//静力水准
                        IOTSensorType.SEDIMENTATION_METER,//沉降仪
                            -> {
                            initval = sensorInfo.initval
                        }

                        IOTSensorType.WEATHER_STATION //气象仪
                            -> {
                            threshold = "99999"
                            corrval = "0"
                        }

                        IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                            -> {
                            initvalx = sensorInfo.initvalx
                            initvaly = sensorInfo.initvaly
                        }

                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE -> {//数字式水位计
                            ropelen = sensorInfo.ropelen
                            tubealti = sensorInfo.tubealti
                        }

                        else -> {
                            // 无额外参数
                        }
                    }
                }
                commands += IOTCommandUtil.getCommand(
                    IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
                    entity.toCommandString()
                )
            }
        return commands
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL -> {
                val result = iotParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        handleCollectorInfo(result.data)
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
                        val errMsg = "查询传感器参数出错: ${result.message}"
                        initEmptySensor()
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
//                        updateFooter()
                    }
                }
            }

            IOTCommandType.MD_RAW -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        // 成功由指令序列完成回调统一处理
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "采集器配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        // 成功场景下由序列完成回调处理提示
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "传感器配置出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        // Toast 在指令序列完成回调中统一处理
                    }
                }
            }

            IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "移除传感器出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("移除成功")
                        updateAdapterRemoveSensorItem()
                    }
                }
            }

            else -> {
                // no-op
            }
        }
    }


    companion object {
        fun newInstance() = DasSensorListFragment()
    }
}
