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
 * 描述：物联网采集器(DAS)振弦式扩展传感器列表页面 - 支持蓝牙通讯方式
 */
class BleDasExternalVibratingSensorListFragment : BaseDasExternalSensorListFragment() {

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
        //##162xxX…X\r\n：设置采集器接入传感器触发阈值
        initTriggerThreshold()
        //##167xxXx…x\r\n：设置振弦式传感器修正参数
        initCorrectionValue()

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

            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> {//传感器触发阈值 162
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

            MDCommandType.VIBRATING_SENSOR_PARAMETER,//传感器修正值 167
            MDCommandType.SENSOR_INSTALLELEVATION -> {//传感器安装高程 169
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
        Timber.Forest.d("设置振弦式采集器接入的传感器===%s", command)
        commandItems.add(command)
    }

    /**
     * 设置采集器接入传感器触发阈值<br/>
     * 指令格式: ##162xxX…X\r\n<br/>
     * xx表示采集器类型，X…X表示阀值，X…X由接入传感器数量N决定（4*N）<br/>
     * 例如：裂缝采集器接入两只拉线位移计，报警值分别30mm、40mm<br/>
     * 设置举例：##1620200300040\r\n<br/>
     * 返回信息：$$1620200300040\r\n<br/>
     */
    private fun initTriggerThreshold() {
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
     * 设置振弦式传感器修正参数<br/>
     * ##167xxXx…x\r\n<br/>
     * xx表示模拟量传感器接入的采集器的通道号取值00~07<br/>
     * X：表示修正参数类型取值'A'，'B'，'C'，'K'，'M'：这些值可以为小数<br/>
     * x…x：为长度不确定的参数<br/>
     */
    private fun initCorrectionValue() {
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEach { sensorAddress ->
                val sensorInfo = mStates.sensorModelMap[sensorAddress]!!

                when (IOTSensorType.Companion.value(sensorInfo.type)) {
                    IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.poly_a}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数A")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.poly_b}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}C${sensorInfo.poly_c}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数C")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}K${sensorInfo.temp_k}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 温度系数K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temp_t0}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.corrval}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.ropelen}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 绳长")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.SENSOR_INSTALLELEVATION,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}${sensorInfo.tubealti}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 安装高程")
                        commandItems.add(command)
                    }

                    IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sens_k}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 灵敏度K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temp_b}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 温度系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referval_f}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temp_t0}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.corrval}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}C${sensorInfo.ropelen}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 绳长")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.SENSOR_INSTALLELEVATION,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}${sensorInfo.tubealti}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 安装高程")
                        commandItems.add(command)
                    }

                    IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sens_k}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 灵敏度K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temp_b}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 温修系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referval_f}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temp_t0}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.corrval}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.elastic_mod}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 膨胀系数")
                        commandItems.add(command)
                    }

                    IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sens_k}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 标定系数A")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temp_b}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 温修系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referval_f}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temp_t0}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.corrval}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 修正值")
                        commandItems.add(command)
                    }

                    else -> {}
                }
            }
    }

    companion object {
        fun newInstance(): BleDasExternalVibratingSensorListFragment {
            return BleDasExternalVibratingSensorListFragment()
        }
    }
}