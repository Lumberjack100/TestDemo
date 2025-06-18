package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
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
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor.BaseBleDasExternalSensorListFragment
import org.koin.android.ext.android.inject
/**
 * 创建者：gonghe
 * 创建时间：2024/6/7
 * 描述：4G通讯模式 - DAS扩展传感器列表页面
 */
class DasExternalSensorListFragment : BaseBleDasExternalSensorListFragment() {
    private val iotParseManager: IOTParserManager by inject()

    /**
     * 查询采集器配置信息
     */
    override fun queryCollectorInfo() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL)
        commandItems.add(command)

        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    override fun queryExtendSensorConfigInfo(sensorNum: Int) {
        commandItems.clear()
        for (i in 0 until sensorNum) {
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR, "index=$i")
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    /**
     * 当接入的传感器个数为0时，设置采集器地址为0，关闭采集器
     */
    override fun closeCollector() {
        commandItems.clear()

        //设置采集器参数
        val entity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            addr = "0",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initSaveCommand() {
        commandItems.clear()

        //设置采集器参数
        val entity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            sensornum = mStates.sensorModelMap.size.toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            entity.toCommandString()
        )
        commandItems.add(command)

        //设置采集器接入的传感器配置信息
        initExtendSensorConfigInfoCommand()

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 设置采集器接入的传感器配置信息
     */
    private fun initExtendSensorConfigInfoCommand() {
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEachIndexed { mIndex, key ->
                val sensorInfo = mStates.sensorModelMap[key]!!
                val entity = DasExternalSensorEntity().apply {
                    index = mIndex.toString()
                    type = sensorInfo.type
                    addr = sensorInfo.addr
                    threshold = sensorInfo.threshold
                    corrval = sensorInfo.corrval
                    when (IOTSensorType.Companion.value(sensorInfo.type)) {
                        IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                            poly_a = sensorInfo.poly_a
                            poly_b = sensorInfo.poly_b
                            poly_c = sensorInfo.poly_c
                            temp_k = sensorInfo.temp_k
                            temp_t0 = sensorInfo.temp_t0
                        }

                        IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                            elastic_mod = sensorInfo.elastic_mod
                        }

                        IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.INCLINOMETER -> {//固定测斜仪
                            spacing = sensorInfo.spacing
                            model_type = sensorInfo.model_type
                        }

                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
                            initvalx = sensorInfo.initvalx
                            initvaly = sensorInfo.initvaly
                            initvalz = sensorInfo.initvalz
                        }

                        IOTSensorType.WEIR -> {//量水堰计
                            lsycsds = sensorInfo.lsycsds
                            lsyysst = sensorInfo.lsyysst
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.STATIC_LEVEL,//静力水准
                        IOTSensorType.SEDIMENTATION_METER,//沉降仪
                        -> {
                            initval = sensorInfo.initval
                        }

                        IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                        -> {
                            initvalx = sensorInfo.initvalx
                            initvaly = sensorInfo.initvaly
                        }

                        IOTSensorType.VIBRATING_SENSOR -> {//MCU_振弦传感器
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE,//数字式水位计
                        IOTSensorType.WATER_LEVEL_GAUGE -> {//MCU_水位(液位)计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                        }

                        IOTSensorType.RADAR_LEVEL_GAUGE -> {//雷达液(物)位计 设置子雷达传感器型号
                            child_type = sensorInfo.child_type
                        }

                        else -> {}
                    }
                }
                val command = IOTCommandUtil.getCommand(
                    IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
                    entity.toCommandString()
                )
                commandItems.add(command)
            }
    }

    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL -> {
                val result = iotParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
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
                        handleFailureResult(errMsg)
                        initEmptySensor()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
                        sendCommandFromCmdList {
                            refreshLayout?.finish()
                            updateFooter()
                        }
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "采集器配置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            if (mStates.sensorModelMap.isEmpty()) {
                                showMessageDialog("采集器地址已修改为0,如继续配置扩展传感器,请先修改采集器地址!")
                            }
                        }
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "传感器配置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                        }
                    }
                }
            }

            IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR -> {//移除传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "移除传感器出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("移除成功")
                            updateAdapterRemoveSensorItem()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }



    companion object {
        fun newInstance() = DasExternalSensorListFragment()
    }
}