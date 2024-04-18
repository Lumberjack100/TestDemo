package com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.device.base.md_cmd.model.das.MDDasExternalSensorInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
class BleDasExternalDigitalSensorListFragment : BaseBleDasExternalSensorListFragment() {

    override fun initSaveCommand() {
        commandDescItems.clear()
        commandItems.clear()

        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        initCollectorSensor()
        initOtherValue()

        val command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendMDCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_40000_MILLIS
        )
    }

    /**
     * 设置采集器接入的传感器<br/>
     * 指令格式: ##150zzxxXXXX\r\n<br/>
     * zz 采集器型号<br/>
     * xx 的取值范围为：01~08，表示接入传感器的个数<br/>
     * 1）当传感器个数为01时XXXX（4个字节）的含义：前两位表示地址或者通道号，后两位表示接入传感器类型<br/>
     * 2）当传感器个数为02时XXXXXXXX（8个字节）的含义：前四位表示第一个地址和对应的传感器类型，后四位表示第二个地址和对应的传感器类型……以此类推。<br/>
     * 该指令不定长，根据接入传感器的个数而定，地址为01~99,通道为00~07<br/>
     */
    private fun initCollectorSensor() {
        val builderFirst = StringBuilder()
        builderFirst.append(
            "${
                MDCommandUtil.formatStringTwo(
                    mStates.collectorType.get()
                )
            }${MDCommandUtil.formatStringTwo(mStates.sensorModelMap.size.toString())}"
        )
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEachIndexed { mIndex, sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                builderFirst.append(
                    "${MDCommandUtil.formatStringTwo(sensorAddress)}${
                        MDCommandUtil.formatStringTwo(
                            sensorInfo.sensorType
                        )
                    }"
                )
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_SENSOR,
            builderFirst.toString()
        )
        Timber.d("设置 %s 接入的传感器===%s", iotSensorType.description, command)
        commandItems.add(command)
    }

    private fun initOtherValue() {
        when (iotSensorType) {
            IOTSensorType.ULTRASONIC_LEVEL_GAUGE //超声波物位计
            -> {
                initMultiTriggerThreshold()
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        initSingleTriggerThreshold(sensorInfo)
                    }
            }

            else -> {
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        initSingleTriggerThreshold(sensorInfo)
                        initSingleCorrectionValue(sensorInfo)
                        //量水堰计需要额外设置
                        if (iotSensorType == IOTSensorType.WEIR) {
                            //初始值指令
                            initSingleInitialReadingValue(sensorInfo)
                            //量水堰计初始堰上水头指令
                            initSingleWeirHeadValue(sensorInfo)
                        }
                        //静力水准/沉降仪 需要额外设置
                        if (iotSensorType == IOTSensorType.STATIC_LEVEL || iotSensorType == IOTSensorType.SEDIMENTATION_METER) {
                            //初始值指令
                            initSingleInitialReadingValue(sensorInfo)
                        }
                    }

                //测斜仪需要额外设置测段长
                if (iotSensorType == IOTSensorType.INCLINOMETER) {
                    initMeasureLongValue()
                }
            }
        }
    }

    /**
     * 超声波物位计 接入传感器触发阈值<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，报警值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private fun initMultiTriggerThreshold() {
        val triggerBuilder = StringBuilder()
        triggerBuilder.append(MDCommandUtil.formatStringTwo(mStates.collectorType.get()))
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEach { sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                triggerBuilder.append(
                    MDCommandUtil.formatStringFour(
                        sensorInfo.triggerThreshold.toIntOrNull()?.toString() ?: "0"
                    )
                )
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI,
            triggerBuilder.toString()
        )
        Timber.d("设置传感器触发阈值===%s", command)
        commandItems.add(command)
    }

    private fun initSingleTriggerThreshold(sensorInfo: MDDasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${sensorInfo.sensorAddress}${sensorInfo.triggerThreshold}"
        )
        commandDescItems.add("地址${sensorInfo.sensorAddress} 触发值")
        commandItems.add(command)
    }

    private fun initSingleCorrectionValue(sensorInfo: MDDasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_REVISED,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${sensorInfo.sensorAddress}${sensorInfo.correctionValue}"
        )
        commandDescItems.add("地址${sensorInfo.sensorAddress} 修正值")
        commandItems.add(command)
    }

    /**
     * 获取初始值指令
     */
    private fun initSingleInitialReadingValue(sensorInfo: MDDasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_INITIAL_READING,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${sensorInfo.sensorAddress}${sensorInfo.lsycsds}"
        )
        commandDescItems.add("地址${sensorInfo.sensorAddress} 初始读数")
        commandItems.add(command)
    }

    /**
     * 获取量水堰计初始堰上水头指令
     */
    private fun initSingleWeirHeadValue(sensorInfo: MDDasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_WEIR_HEAD,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${sensorInfo.sensorAddress}${sensorInfo.lsyysst}"
        )
        commandDescItems.add("地址${sensorInfo.sensorAddress} 初始堰上水头")
        commandItems.add(command)
    }

    /**
     * 获取测斜仪的测段长指令
     */
    private fun initMeasureLongValue() {
        val triggerBuilder = StringBuilder()
        triggerBuilder.append(MDCommandUtil.formatStringTwo(mStates.collectorType.get()))
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEach { sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                triggerBuilder.append(
                    MDCommandUtil.formatStringFive(
                        sensorInfo.measuringSectionLength.toIntOrNull()?.toString() ?: "0"
                    )
                )
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_INCLINOMETER_LONG,
            triggerBuilder.toString()
        )
        Timber.d("设置测斜仪测段长===%s", command)
        commandItems.add(command)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> {//传感器触发阈值(多传感器设置) 162
                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "传感器触发阈值配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        val commandDesc = if (commandDescItems.isEmpty()) "修正值" else {
                            commandDescItems.first
                        }
                        Timber.d("设置$commandDesc")
                        sendMDCommandFromCmdList()
                    }
                }
            }
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE -> {//传感器触发阈值(单传感器设置) 168
                var commandDesc = if (commandDescItems.isEmpty()) "触发值" else {
                    commandDescItems.first
                    commandDescItems.removeFirst()
                }

                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "$commandDesc 配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        commandDesc = if (commandDescItems.isEmpty()) "" else {
                            commandDescItems.first
                        }
                        Timber.d("设置$commandDesc")
                        sendMDCommandFromCmdList()
                    }
                }
            }
            MDCommandType.COLLECTOR_SENSOR_REVISED -> {//传感器修正值 165
                var commandDesc = if (commandDescItems.isEmpty()) "修正值" else {
                    commandDescItems.first
                    commandDescItems.removeFirst()
                }

                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "$commandDesc 配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        commandDesc = if (commandDescItems.isEmpty()) "" else {
                            commandDescItems.first
                        }
                        Timber.d("设置$commandDesc")
                        sendMDCommandFromCmdList()
                    }
                }
            }
            MDCommandType.SENSOR_INITIAL_READING -> {//设置量水堰初始读数(单传感器设置) 171
                var commandDesc = if (commandDescItems.isEmpty()) "初始读数" else {
                    commandDescItems.first
                    commandDescItems.removeFirst()
                }

                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "$commandDesc 配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        commandDesc = if (commandDescItems.isEmpty()) "" else {
                            commandDescItems.first
                        }
                        Timber.d("设置$commandDesc")
                        sendMDCommandFromCmdList()
                    }
                }
            }
            MDCommandType.SENSOR_WEIR_HEAD -> {//设置量水堰初始堰上水头(单传感器设置) 172
                var commandDesc = if (commandDescItems.isEmpty()) "初始堰上水头" else {
                    commandDescItems.first
                    commandDescItems.removeFirst()
                }

                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "$commandDesc 配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        commandDesc = if (commandDescItems.isEmpty()) "" else {
                            commandDescItems.first
                        }
                        Timber.d("设置$commandDesc")
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_INCLINOMETER_LONG -> {//设置测斜仪测段长 166
                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "传感器测段长配置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            else -> {
                super.setResultData(cmdStr)
            }
        }
    }

    companion object {
        fun newInstance(): BleDasExternalDigitalSensorListFragment {
            return BleDasExternalDigitalSensorListFragment()
        }
    }
}