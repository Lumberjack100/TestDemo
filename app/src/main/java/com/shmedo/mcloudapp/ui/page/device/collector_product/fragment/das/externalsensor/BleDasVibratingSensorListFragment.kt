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
import com.shmedo.mcloudapp.extensions.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述：物联网采集器(DAS)振弦式扩展传感器列表页面 - 支持蓝牙通讯方式
 */
class BleDasVibratingSensorListFragment : BaseDasSensorListFragment() {

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

        //##150zzxxXXXX\r\n：设置采集器接入的传感器
        initCollectorSensor(commands)
        //##162xxX…X\r\n：设置采集器接入传感器触发阈值
        initTriggerThreshold(commands)
        //##167xxXx…x\r\n：设置振弦式传感器修正参数
        initCorrectionValue(commands)

        commands += MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )

        if (commands.isEmpty()) {
            return
        }

        saveCompletionAction = {
            Toaster.show("保存成功")
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
//                        updateFooter()
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

            MDCommandType.SAVE_CONFIG_INFO -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("保存出错!")
                    }

                    else -> {
                        // 成功提示由序列完成回调统一处理
                    }
                }
            }

            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> {//传感器触发阈值 162
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("传感器触发阈值配置出错", isMessageDialog = true)
                    }

                    else -> {
                        val commandDesc = commandDescriptions.firstOrNull() ?: "修正值"
                        Timber.d("设置$commandDesc")
                    }
                }
            }

            MDCommandType.VIBRATING_SENSOR_PARAMETER,//传感器修正值 167
            MDCommandType.SENSOR_INSTALLELEVATION -> {//传感器安装高程 169
                var commandDesc = if (commandDescriptions.isEmpty()) {
                    "修正值"
                } else {
                    commandDescriptions.removeFirst()
                }

                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        handleFailureResult("$commandDesc 配置出错", isMessageDialog = true)
                    }

                    else -> {
                        commandDesc = commandDescriptions.firstOrNull() ?: ""
                        Timber.d("设置$commandDesc")
                    }
                }
            }

            else -> {
                // 其他指令类型暂不处理
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
        Timber.d("设置振弦式采集器接入的传感器===%s", command)
        commands += command
    }

    /**
     * 设置采集器接入传感器触发阈值<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，报警值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private fun initTriggerThreshold(commands: MutableList<String>) {
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
     * 设置振弦式传感器修正参数<br/>
     * ##167xxXx…x\r\n<br/>
     * xx表示模拟量传感器接入的采集器的通道号取值00~07<br/>
     * X：表示修正参数类型取值'A'，'B'，'C'，'K'，'M'：这些值可以为小数<br/>
     * x…x：为长度不确定的参数<br/>
     */
    private fun initCorrectionValue(commands: MutableList<String>) {
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEach { sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!
                val channel = MDCommandUtil.formatStringTwo(sensorAddress)

                fun addParameterCommand(type: Char, value: String, desc: String) {
                    val command = MDCommandUtil.getCommand(
                        MDCommandType.VIBRATING_SENSOR_PARAMETER,
                        "$channel$type$value"
                    )
                    commandDescriptions.addLast("通道$sensorAddress $desc")
                    commands += command
                }

                fun addElevationCommand(value: String, desc: String) {
                    val command = MDCommandUtil.getCommand(
                        MDCommandType.SENSOR_INSTALLELEVATION,
                        "$channel$value"
                    )
                    commandDescriptions.addLast("通道$sensorAddress $desc")
                    commands += command
                }

                when (IOTSensorType.value(sensorInfo.type)) {
                    IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                        addParameterCommand('A', sensorInfo.poly_a, "基康渗压计 多项式系数A")
                        addParameterCommand('B', sensorInfo.poly_b, "基康渗压计 多项式系数B")
                        addParameterCommand('C', sensorInfo.poly_c, "基康渗压计 多项式系数C")
                        addParameterCommand('K', sensorInfo.temp_k, "基康渗压计 温度系数K")
                        addParameterCommand('T', sensorInfo.temp_t0, "基康渗压计 初始温度T0")
                        addParameterCommand('M', sensorInfo.corrval, "基康渗压计 修正值")
                        addParameterCommand('F', sensorInfo.ropelen, "基康渗压计 绳长")
                        addElevationCommand(sensorInfo.tubealti, "基康渗压计 安装高程")
                    }

                    IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                        addParameterCommand('A', sensorInfo.sens_k, "葛南渗压计 灵敏度K")
                        addParameterCommand('B', sensorInfo.temp_b, "葛南渗压计 温度系数B")
                        addParameterCommand('F', sensorInfo.referval_f, "葛南渗压计 基准值F0")
                        addParameterCommand('T', sensorInfo.temp_t0, "葛南渗压计 初始温度T0")
                        addParameterCommand('M', sensorInfo.corrval, "葛南渗压计 修正值")
                        addParameterCommand('C', sensorInfo.ropelen, "葛南渗压计 绳长")
                        addElevationCommand(sensorInfo.tubealti, "葛南渗压计 安装高程")
                    }

                    IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                        addParameterCommand('A', sensorInfo.sens_k, "应力计 灵敏度K")
                        addParameterCommand('B', sensorInfo.temp_b, "应力计 温修系数B")
                        addParameterCommand('F', sensorInfo.referval_f, "应力计 基准值F0")
                        addParameterCommand('T', sensorInfo.temp_t0, "应力计 初始温度T0")
                        addParameterCommand('M', sensorInfo.corrval, "应力计 修正值")
                        addParameterCommand('M', sensorInfo.elastic_mod, "应力计 膨胀系数")
                    }

                    IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                        addParameterCommand('A', sensorInfo.sens_k, "轴力计 标定系数A")
                        addParameterCommand('B', sensorInfo.temp_b, "轴力计 温修系数B")
                        addParameterCommand('F', sensorInfo.referval_f, "轴力计 基准值F0")
                        addParameterCommand('T', sensorInfo.temp_t0, "轴力计 初始温度T0")
                        addParameterCommand('M', sensorInfo.corrval, "轴力计 修正值")
                    }

                    else -> {
                        // 其他传感器类型无需处理
                    }
                }
            }
    }

    companion object {
        fun newInstance(): BleDasVibratingSensorListFragment {
            return BleDasVibratingSensorListFragment()
        }
    }
}
