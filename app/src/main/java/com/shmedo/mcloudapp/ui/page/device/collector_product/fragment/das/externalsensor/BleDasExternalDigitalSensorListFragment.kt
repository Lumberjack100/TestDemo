package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述：物联网采集器(DAS)数字式扩展传感器列表页面 - 支持蓝牙通讯方式
 */
class BleDasExternalDigitalSensorListFragment : BaseDasExternalSensorListFragment() {

    private val mdParseManager: MDParserManager by inject()

    override fun queryCollectorInfo() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CONFIG,
            MDCommandUtil.formatStringTwo(mStates.collectorType.get())
        )
        commandItems.add(command)
        Timber.Forest.d("查询采集器配置信息===%s", command)

        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    override fun queryExtendSensorConfigInfo(sensorNum: Int) {
        commandItems.clear()
        for (index in 0 until sensorNum) {
            val model = MDCommandUtil.formatStringTwo(mStates.collectorType.get())
            val address = MDCommandUtil.formatStringTwo(index.toString())
            val command = MDCommandUtil.getCommand(
                MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
                "$model$address"
            )
            commandItems.add(command)
            Timber.Forest.d(
                "获取 %s 采集器 %s 通道的传感器参数===%s",
                IOTSensorType.Companion.value(mStates.collectorType.get()),
                address,
                command
            )
        }
        sendCommandFromCmdList()
    }

    override fun closeCollector() {
        commandItems.clear()
        var command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_ADDRESS,
            "0"
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 删除传感器
     */
    override fun onDeleteSensor() {
        updateAdapterRemoveSensorItem()
    }

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
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_40000_MILLIS
        )
    }

    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_CONFIG -> {
                val result = mdParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("查询采集器参数出错")
                        return
                    }
                    is MDCommandResult.Success -> {
                        handleCollectorInfo(result.data)
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
                        handleFailureResult("查询传感器参数出错")
                        initEmptySensor()
                        return
                    }
                    is MDCommandResult.Success -> {
                        processSensorParamsInfo(result.data)
                        sendCommandFromCmdList {
                            refreshLayout?.finish()
                            updateFooter()
                        }
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_ADDRESS -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("采集器地址配置出错!")
                        return
                    }
                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSOR -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("传感器配置出错")
                        return
                    }
                    else -> {
                        val commandDesc = if (commandDescItems.isEmpty()) "触发值" else {
                            commandDescItems.first
                        }
                        Timber.Forest.d("设置$commandDesc")
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("保存出错!")
                        return
                    }
                    else -> {
                        sendCommandFromCmdList {
                            if (mStates.sensorModelMap.isEmpty()) {
                                showMessageDialog("采集器地址已修改为0,如继续配置扩展传感器,请先修改采集器地址!")
                            } else {
                                Toaster.show("数据保存成功")
                            }
                        }
                    }
                }
            }

            // 处理其他具体的传感器配置命令
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> handleTriggerThresholdMultiResult(cmdStr)
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE -> handleTriggerThresholdSingleResult(cmdStr)
            MDCommandType.COLLECTOR_SENSOR_REVISED -> handleCorrectionValueResult(cmdStr)
            MDCommandType.SENSOR_INITIAL_READING -> handleInitialReadingResult(cmdStr)
            MDCommandType.SENSOR_WEIR_HEAD -> handleWeirHeadResult(cmdStr)
            MDCommandType.SET_INCLINOMETER_LONG -> handleInclinometerLongResult(cmdStr)

            else -> {
                // 不处理的命令类型
            }
        }
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
                            sensorInfo.type
                        )
                    }"
                )
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_SENSOR,
            builderFirst.toString()
        )
        Timber.Forest.d("设置 %s 接入的传感器===%s", iotSensorType.description, command)
        commandItems.add(command)
    }

    private fun initOtherValue() {
        when (iotSensorType) {
            IOTSensorType.ULTRASONIC_LEVEL_GAUGE //超声波物位计
            -> {
                //触发值
                initMultiTriggerThreshold()
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        //修正值
                        initSingleCorrectionValue(sensorInfo)
                    }
            }

            else -> {
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        when (iotSensorType) {
                            IOTSensorType.WEIR //量水堰计
                            -> {
                                //触发值
                                initSingleTriggerThreshold(sensorInfo)
                                //修正值
                                initSingleCorrectionValue(sensorInfo)
                                //初始读数
                                initSingleInitialReadingValue(sensorInfo.addr, sensorInfo.lsycsds)
                                //初始堰上水头
                                initSingleWeirHeadValue(sensorInfo)
                            }

                            IOTSensorType.STATIC_LEVEL,//静力水准
                            IOTSensorType.SEDIMENTATION_METER,//沉降仪
                            -> {
                                //触发值
                                initSingleTriggerThreshold(sensorInfo)
                                //修正值
                                initSingleCorrectionValue(sensorInfo)
                                //初始值
                                initSingleInitialReadingValue(sensorInfo.addr, sensorInfo.initval)
                            }

                            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                            -> {
                                //触发值
                                initSingleTriggerThreshold(sensorInfo)
                                //X轴初始值、Y轴初始值
                                initSingleInitialReadingValue2(
                                    sensorInfo.addr,
                                    sensorInfo.initvalx,
                                    sensorInfo.initvaly
                                )
                            }

                            else -> {
                                //触发值
                                initSingleTriggerThreshold(sensorInfo)
                                //修正值
                                initSingleCorrectionValue(sensorInfo)
                            }
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
                    MDCommandUtil.formatStringFour(sensorInfo.threshold)
                )
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI,
            triggerBuilder.toString()
        )
        Timber.Forest.d("设置传感器触发阈值===%s", command)
        commandItems.add(command)
    }

    /**
     * 接入传感器触发值<br/>
     * 指令格式: cxx\r\n<br/>
     */
    private fun initSingleTriggerThreshold(sensorInfo: DasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.threshold}"
        )
        commandDescItems.add("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 触发值")
        commandItems.add(command)
    }

    /**
     * 接入传感器修正值<br/>
     * 指令格式: ##165xx\r\n<br/>
     */
    private fun initSingleCorrectionValue(sensorInfo: DasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_REVISED,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.corrval}"
        )
        commandDescItems.add("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 修正值")
        commandItems.add(command)
    }

    /**
     * 设置 量水堰计初始读数、静力水准/沉降仪初始值 指令
     * 指令格式: ##171xx\r\n<br/>
     */
    private fun initSingleInitialReadingValue(address: String, value: String) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_INITIAL_READING,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    address
                )
            }${value}"
        )
        commandDescItems.add("地址${MDCommandUtil.formatStringTwo(address)} 初始读数/初始值")
        commandItems.add(command)
    }

    /**
     * 设置 垂线坐标仪初始值 指令
     * 指令格式: ##165xx\r\n<br/>
     */
    private fun initSingleInitialReadingValue2(
        address: String,
        initvalx: String,
        initvaly: String
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_REVISED,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${MDCommandUtil.formatStringTwo(address)}${initvalx},${initvaly}"
        )
        commandDescItems.add("地址${MDCommandUtil.formatStringTwo(address)} 初始值")
        commandItems.add(command)
    }

    /**
     * 设置量水堰计初始堰上水头指令
     * 指令格式: ##172xx\r\n<br/>
     */
    private fun initSingleWeirHeadValue(sensorInfo: DasExternalSensorInfo) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_WEIR_HEAD,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.lsyysst}"
        )
        commandDescItems.add("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 初始堰上水头")
        commandItems.add(command)
    }

    /**
     * 设置测斜仪的测段长指令
     * 指令格式: ##166xx\r\n<br/>
     */
    private fun initMeasureLongValue() {
        val triggerBuilder = StringBuilder()
        triggerBuilder.append(MDCommandUtil.formatStringTwo(mStates.collectorType.get()))
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEach { sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                triggerBuilder.append(MDCommandUtil.formatStringFive(sensorInfo.spacing))
            }

        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_INCLINOMETER_LONG,
            triggerBuilder.toString()
        )
        Timber.Forest.d("设置测斜仪测段长===%s", command)
        commandItems.add(command)
    }

    private fun handleTriggerThresholdMultiResult(cmdStr: String) {
        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "传感器触发阈值配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                val commandDesc = if (commandDescItems.isEmpty()) "修正值" else {
                    commandDescItems.first
                }
                Timber.Forest.d("设置$commandDesc")
                sendCommandFromCmdList()
            }
        }
    }

    private fun handleTriggerThresholdSingleResult(cmdStr: String) {
        var commandDesc = if (commandDescItems.isEmpty()) "触发值" else {
            commandDescItems.first
            commandDescItems.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                commandDesc = if (commandDescItems.isEmpty()) "" else {
                    commandDescItems.first
                }
                Timber.Forest.d("设置$commandDesc")
                sendCommandFromCmdList()
            }
        }
    }

    private fun handleCorrectionValueResult(cmdStr: String) {
        var commandDesc = if (commandDescItems.isEmpty()) "修正值" else {
            commandDescItems.first
            commandDescItems.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                commandDesc = if (commandDescItems.isEmpty()) "" else {
                    commandDescItems.first
                }
                Timber.Forest.d("设置$commandDesc")
                sendCommandFromCmdList()
            }
        }
    }

    private fun handleInitialReadingResult(cmdStr: String) {
        var commandDesc = if (commandDescItems.isEmpty()) "初始读数" else {
            commandDescItems.first
            commandDescItems.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                commandDesc = if (commandDescItems.isEmpty()) "" else {
                    commandDescItems.first
                }
                Timber.Forest.d("设置$commandDesc")
                sendCommandFromCmdList()
            }
        }
    }

    private fun handleWeirHeadResult(cmdStr: String) {
        var commandDesc = if (commandDescItems.isEmpty()) "初始堰上水头" else {
            commandDescItems.first
            commandDescItems.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                commandDesc = if (commandDescItems.isEmpty()) "" else {
                    commandDescItems.first
                }
                Timber.Forest.d("设置$commandDesc")
                sendCommandFromCmdList()
            }
        }
    }

    private fun handleInclinometerLongResult(cmdStr: String) {
        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "传感器测段长配置出错"
                handleFailureResult(errMsg)
                return
            }
            else -> {
                sendCommandFromCmdList()
            }
        }
    }

    companion object {
        fun newInstance(): BleDasExternalDigitalSensorListFragment {
            return BleDasExternalDigitalSensorListFragment()
        }
    }
}