package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述：物联网采集器(DAS)数字式扩展传感器列表页面 - 支持蓝牙通讯方式
 */
class BleDasDigitalSensorListFragment : BaseDasSensorListFragment() {

    private val mdParseManager: MDParserManager by inject()
    private val commandDescriptions = ArrayDeque<String>()
    private var saveCompletionAction: (() -> Unit)? = null

    override fun queryCollectorInfo() {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CONFIG,
            MDCommandUtil.formatStringTwo(mStates.collectorType.get())
        )
        Timber.d("查询采集器配置信息===%s", command)

        executeCollectorInfoCommands(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun queryExtendSensorConfigInfo(sensorNum: Int) {
        val commands = buildList {
            for (index in 0 until sensorNum) {
                val model = MDCommandUtil.formatStringTwo(mStates.collectorType.get())
                val address = MDCommandUtil.formatStringTwo(index.toString())
                val command = MDCommandUtil.getCommand(
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
                    "$model$address"
                )
                add(command)
                Timber.d(
                    "获取 %s 采集器 %s 通道的传感器参数===%s",
                    IOTSensorType.value(mStates.collectorType.get()),
                    address,
                    command
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

    override fun closeCollector() {
        val commands = listOf(
            MDCommandUtil.getCommand(
                MDCommandType.SET_COLLECTOR_ADDRESS,
                "0"
            ),
            MDCommandUtil.getCommand(
                MDCommandType.SAVE_CONFIG_INFO,
                SaveConfigMode.SAVE_NO_REBOOT.toString()
            )
        )

        sendCommandSequence(
            commands = commands,
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
        updateAdapterRemoveSensorItem()
    }

    override fun initSaveCommand() {
        commandDescriptions.clear()
        val commands = mutableListOf<String>()

        val isWeatherStation =
            IOTSensorType.value(mStates.collectorType.get()) == IOTSensorType.WEATHER_STATION

        if (isWeatherStation) {
            val command = MDCommandUtil.getCommand(MDCommandType.CHOOSE_SENSOR_MANUFACTURER, "4")
            Timber.d("多要素气象计选择传感器厂家===%s", command)
            commands += command
        }

        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        initCollectorSensor(commands)
        initOtherValue(commands)

        commands += MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )

        if (isWeatherStation) {
            commands += MDCommandUtil.getCommand(
                MDCommandType.REBOOT_DEVICE,
                "1"
            )
        }

        if (commands.isEmpty()) {
            return
        }

        saveCompletionAction = when {
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
                    saveCompletionAction?.invoke()
                    saveCompletionAction = null
                },
                onError = { _, _ ->
                    saveCompletionAction = null
                }
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_CONFIG -> {
                val result = mdParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("查询采集器参数出错", isMessageDialog = true)
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
                        initEmptySensor()
                        handleFailureResult("查询传感器参数出错", isMessageDialog = true)
                    }

                    is MDCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_ADDRESS -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("采集器地址配置出错!", isMessageDialog = true)
                    }

                    else -> {
                        // no-op
                    }
                }
            }

            MDCommandType.CHOOSE_SENSOR_MANUFACTURER -> {//MR701H-多要素气象计选择传感器厂家
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("传感器配置出错!", isMessageDialog = true)
                    }

                    else -> {
                        // no-op
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSOR -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("传感器配置出错", isMessageDialog = true)
                    }

                    else -> {
                        val commandDesc = commandDescriptions.firstOrNull() ?: "触发值"
                        Timber.d("设置$commandDesc")
                    }
                }
            }

            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> handleTriggerThresholdMultiResult(
                cmdStr
            )

            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE -> handleTriggerThresholdSingleResult(
                cmdStr
            )

            MDCommandType.COLLECTOR_SENSOR_REVISED -> handleCorrectionValueResult(cmdStr)

            MDCommandType.SENSOR_INITIAL_READING -> handleInitialReadingResult(cmdStr)

            MDCommandType.SENSOR_WEIR_HEAD -> handleWeirHeadResult(cmdStr)

            MDCommandType.SET_INCLINOMETER_LONG -> handleInclinometerLongResult(cmdStr)

            MDCommandType.SAVE_CONFIG_INFO -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("保存出错!", isMessageDialog = true)
                    }

                    else -> {
                        // 成功消息由序列完成回调统一处理
                    }
                }
            }

            MDCommandType.REBOOT_DEVICE -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("保存出错!", isMessageDialog = true)
                    }

                    else -> {
                        // 成功消息由序列完成回调统一处理
                    }
                }
            }

            else -> {
                // 其他指令类型无需处理
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
    private fun initCollectorSensor(commands: MutableList<String>) {
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


        Timber.d(
            "设置 %s 接入的传感器===%s",
            IOTSensorType.value(mStates.collectorType.get()).description,
            command
        )
        commands += command
    }

    private fun initOtherValue(commands: MutableList<String>) {
        when (IOTSensorType.value(mStates.collectorType.get())) {
            IOTSensorType.ULTRASONIC_LEVEL_GAUGE //超声波物位计
                -> {
                //触发值
                initMultiTriggerThreshold(commands)
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        //修正值
                        initSingleCorrectionValue(commands, sensorInfo)
                    }
            }

            else -> {
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
                    .forEach { sensorAddress ->
                        val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                        when (IOTSensorType.value(mStates.collectorType.get())) {
                            IOTSensorType.WEIR //量水堰计
                                -> {
                                //触发值
                                initSingleTriggerThreshold(commands, sensorInfo)
                                //修正值
                                initSingleCorrectionValue(commands, sensorInfo)
                                //初始读数
                                initSingleInitialReadingValue(
                                    commands,
                                    sensorInfo.addr,
                                    sensorInfo.lsycsds
                                )
                                //初始堰上水头
                                initSingleWeirHeadValue(commands, sensorInfo)
                            }

                            IOTSensorType.STATIC_LEVEL,//静力水准
                            IOTSensorType.SEDIMENTATION_METER,//沉降仪
                                -> {
                                //触发值
                                initSingleTriggerThreshold(commands, sensorInfo)
                                //修正值
                                initSingleCorrectionValue(commands, sensorInfo)
                                //初始值
                                initSingleInitialReadingValue(
                                    commands,
                                    sensorInfo.addr,
                                    sensorInfo.initval
                                )
                            }

                            IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                                -> {
                                //触发值
                                initSingleTriggerThreshold(commands, sensorInfo)
                                //X轴初始值、Y轴初始值
                                initSingleInitialReadingValue2(
                                    commands,
                                    sensorInfo.addr,
                                    "${sensorInfo.initvalx},${sensorInfo.initvaly}"
                                )
                            }

                            IOTSensorType.LUYAN_INCLINOMETER,//倾角仪
                                -> {
                                //触发值
                                initSingleTriggerThreshold(commands, sensorInfo)
                                //X轴初始值、Y轴初始值
                                initSingleInitialReadingValue2(
                                    commands,
                                    sensorInfo.addr,
                                    "${sensorInfo.initvalx},${sensorInfo.initvaly},${sensorInfo.initvalz}"
                                )
                            }

                            else -> {
                                //触发值
                                initSingleTriggerThreshold(commands, sensorInfo)
                                //修正值
                                initSingleCorrectionValue(commands, sensorInfo)
                            }
                        }
                    }

                //测斜仪需要额外设置测段长
                if (IOTSensorType.value(mStates.collectorType.get()) == IOTSensorType.INCLINOMETER) {
                    initMeasureLongValue(commands)
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
    private fun initMultiTriggerThreshold(commands: MutableList<String>) {
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
        Timber.d("设置传感器触发阈值===%s", command)
        commands += command
    }

    /**
     * 接入传感器触发值<br/>
     * 指令格式: cxx\r\n<br/>
     */
    private fun initSingleTriggerThreshold(
        commands: MutableList<String>,
        sensorInfo: DasExternalSensorInfo
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_SINGLE,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.threshold}"
        )
        commandDescriptions.addLast("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 触发值")
        commands += command
    }

    /**
     * 接入传感器修正值<br/>
     * 指令格式: ##165xx\r\n<br/>
     */
    private fun initSingleCorrectionValue(
        commands: MutableList<String>,
        sensorInfo: DasExternalSensorInfo
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_REVISED,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.corrval}"
        )
        commandDescriptions.addLast("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 修正值")
        commands += command
    }

    /**
     * 设置 量水堰计初始读数、静力水准/沉降仪初始值 指令
     * 指令格式: ##171xx\r\n<br/>
     */
    private fun initSingleInitialReadingValue(
        commands: MutableList<String>,
        address: String,
        value: String
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_INITIAL_READING,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    address
                )
            }${value}"
        )
        commandDescriptions.addLast("地址${MDCommandUtil.formatStringTwo(address)} 初始读数/初始值")
        commands += command
    }

    /**
     * 设置 垂线坐标仪初始值 指令
     * 指令格式: ##165xx\r\n<br/>
     */
    private fun initSingleInitialReadingValue2(
        commands: MutableList<String>,
        address: String,
        initValue: String
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SENSOR_REVISED,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    address
                )
            }${initValue}"
        )
        commandDescriptions.addLast("地址${MDCommandUtil.formatStringTwo(address)} 初始值")
        commands += command
    }

    /**
     * 设置量水堰计初始堰上水头指令
     * 指令格式: ##172xx\r\n<br/>
     */
    private fun initSingleWeirHeadValue(
        commands: MutableList<String>,
        sensorInfo: DasExternalSensorInfo
    ) {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SENSOR_WEIR_HEAD,
            "${MDCommandUtil.formatStringTwo(mStates.collectorType.get())}${
                MDCommandUtil.formatStringTwo(
                    sensorInfo.addr
                )
            }${sensorInfo.lsyysst}"
        )
        commandDescriptions.addLast("地址${MDCommandUtil.formatStringTwo(sensorInfo.addr)} 初始堰上水头")
        commands += command
    }

    /**
     * 设置测斜仪的测段长指令
     * 指令格式: ##166xx\r\n<br/>
     */
    private fun initMeasureLongValue(commands: MutableList<String>) {
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
        Timber.d("设置测斜仪测段长===%s", command)
        commands += command
    }

    private fun handleTriggerThresholdMultiResult(cmdStr: String) {
        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "传感器触发阈值配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                val commandDesc = commandDescriptions.firstOrNull() ?: "修正值"
                Timber.d("设置$commandDesc")
            }
        }
    }

    private fun handleTriggerThresholdSingleResult(cmdStr: String) {
        var commandDesc = if (commandDescriptions.isEmpty()) {
            "触发值"
        } else {
            commandDescriptions.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                commandDesc = commandDescriptions.firstOrNull() ?: ""
                Timber.d("设置$commandDesc")
            }
        }
    }

    private fun handleCorrectionValueResult(cmdStr: String) {
        var commandDesc = if (commandDescriptions.isEmpty()) {
            "修正值"
        } else {
            commandDescriptions.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                commandDesc = commandDescriptions.firstOrNull() ?: ""
                Timber.d("设置$commandDesc")
            }
        }
    }

    private fun handleInitialReadingResult(cmdStr: String) {
        var commandDesc = if (commandDescriptions.isEmpty()) {
            "初始读数"
        } else {
            commandDescriptions.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                commandDesc = commandDescriptions.firstOrNull() ?: ""
                Timber.d("设置$commandDesc")
            }
        }
    }

    private fun handleWeirHeadResult(cmdStr: String) {
        var commandDesc = if (commandDescriptions.isEmpty()) {
            "初始堰上水头"
        } else {
            commandDescriptions.removeFirst()
        }

        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "$commandDesc 配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                commandDesc = commandDescriptions.firstOrNull() ?: ""
                Timber.d("设置$commandDesc")
            }
        }
    }

    private fun handleInclinometerLongResult(cmdStr: String) {
        when (val result = mdParseManager.parse<String>(cmdStr)) {
            is MDCommandResult.Failure -> {
                val errMsg = "传感器测段长配置出错"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                // 成功后无需额外处理
            }
        }
    }

    companion object {
        fun newInstance(): BleDasDigitalSensorListFragment {
            return BleDasDigitalSensorListFragment()
        }
    }
}
