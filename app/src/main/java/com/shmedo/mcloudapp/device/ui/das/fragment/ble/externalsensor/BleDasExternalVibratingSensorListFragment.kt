package com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor

import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showLoadingDialog
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
class BleDasExternalVibratingSensorListFragment : BaseBleDasExternalSensorListFragment() {


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
        Timber.d("设置振弦式采集器接入的传感器===%s", command)
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

                when (IOTSensorType.value(sensorInfo.sensorType)) {
                    IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.polynomialRatioA}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数A")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.polynomialRatioB}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}C${sensorInfo.polynomialRatioC}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 多项式系数C")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}K${sensorInfo.temperatureCoefficientK}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 温度系数K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temperatureCoefficientT0}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.correctionValue}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.wireRopeLength}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 绳长")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.SENSOR_INSTALLELEVATION,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}${sensorInfo.installElevation}"
                        )
                        commandDescItems.add("通道$sensorAddress 基康渗压计 安装高程")
                        commandItems.add(command)
                    }

                    IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sensitivityK}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 灵敏度K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temperatureCoefficientB}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 温度系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referenceValueF}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temperatureCoefficientT0}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.correctionValue}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}C${sensorInfo.wireRopeLength}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 绳长")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.SENSOR_INSTALLELEVATION,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}${sensorInfo.installElevation}"
                        )
                        commandDescItems.add("通道$sensorAddress 葛南渗压计 安装高程")
                        commandItems.add(command)
                    }

                    IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sensitivityK}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 灵敏度K")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temperatureCoefficientB}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 温修系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referenceValueF}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temperatureCoefficientT0}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.correctionValue}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 修正值")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.expansionCoefficient}"
                        )
                        commandDescItems.add("通道$sensorAddress 应力计 膨胀系数")
                        commandItems.add(command)
                    }

                    IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                        var command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}A${sensorInfo.sensitivityK}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 标定系数A")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}B${sensorInfo.temperatureCoefficientB}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 温修系数B")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}F${sensorInfo.referenceValueF}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 基准值F0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}T${sensorInfo.temperatureCoefficientT0}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 初始温度T0")
                        commandItems.add(command)

                        command = MDCommandUtil.getCommand(
                            MDCommandType.VIBRATING_SENSOR_PARAMETER,
                            "${MDCommandUtil.formatStringTwo(sensorAddress)}M${sensorInfo.correctionValue}"
                        )
                        commandDescItems.add("通道$sensorAddress 轴力计 修正值")
                        commandItems.add(command)
                    }

                    else -> {}
                }
            }
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_SENSOR_THRESHOLD_MULTI -> {//传感器触发阈值 162
                when (val result = mdParseManager.parse<String>(cmdStr)) {
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
                        sendCommandFromCmdList()
                    }
                }
            }

            else -> {
                super.setResultData(cmdStr)
            }
        }
    }

    companion object {
        fun newInstance(): BleDasExternalVibratingSensorListFragment {
            return BleDasExternalVibratingSensorListFragment()
        }
    }
}