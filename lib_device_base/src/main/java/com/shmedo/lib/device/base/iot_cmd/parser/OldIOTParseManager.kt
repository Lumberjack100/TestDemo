package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.OldIOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeAnthropomorphicMovementInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBaseInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBasicConfigParamParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBrakePadControlInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeCurrentStateInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeExecutiveAgencyInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeGuideGrooveCalibrationInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeInclinometerInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeLockedRotorDetectionInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeLowEnergyModeInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMeasuringHoleDepthInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMeterWheelParamParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotionStateParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotorMotionAngleInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotorMotionDistanceInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotorPowerInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeStepperMotorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeVoltageConfigParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeWorkModeParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.AudibleAlarmParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasBaseInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasBdTerminalInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasCollectorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasDataReportInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasDigitalPiezometerInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasExternalSensorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasFixedPointReportInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasIOSensorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasNetStatusInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasSensorStatusInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasSolarStatusInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasSubSensorStatusInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.DasTemperatureAndHumidityStatusinfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.das.McuAddressParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40BasePositionInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40BoardSolutionInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40CORSInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40EthernetInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40GpsWorkInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40NmeaTimeInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40RTKModeInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40SatelitteInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.e40.E40SerialPortInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacExecutiveAgencyInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacMeasuringDataInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacMeasuringHoleDepthInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacMotionStateParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacMotorMotionDistanceInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.hac.HacWarningValueParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.lr200.LR200PositionInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.rn20.Rn20BaseInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.rn20.Rn20ModuleStatusParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.rn20.Rn20PositionInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.TerminalTelemetryParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsAisleInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsAisleTerminalInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsBasicInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsTerminalCollectorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsTerminalCommInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsTerminalSensorInfoParserOld
import com.shmedo.lib.device.base.iot_cmd.parser.vms.VmsTerminalSnParserOld
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil.extractCommandType
import java.util.EnumMap

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     TODO #gh#
 */
class OldIOTParseManager private constructor() {
    private val parserMap: MutableMap<IOTCommandType, OldIOTResultParser<*>> = EnumMap(IOTCommandType::class.java)

    init {
        registerParse()
    }

    fun <T> parse(result: String): OldIOTCommandResult<T> {
        baseValidate(result)
        val tempCmd = result.replace("&&", "")
        val commandResult = OldIOTCommandResult<T>()

        //失败的指令处理
        if (tempCmd.contains(OldIOTCommandResult.ERROR_FLAG)) {
            val settingCmdResult = OldCommonSettingCmdResultParserOld.instance.parse(result)
            commandResult.isSuccess = false
            commandResult.message = settingCmdResult.reason
            return commandResult
        }
        val cmdType = extractCommandType(tempCmd)
        val parser = parserMap[cmdType]
        if (parser == null) {
            commandResult.isSuccess = false
            commandResult.message = "未找到命令：" + cmdType.toString() + "的解析器"
            commandResult.commandType = cmdType
            return commandResult
        }
        parser.validate(tempCmd)
        val data = parser.parse(tempCmd) as T
        commandResult.isSuccess = true
        commandResult.commandType = cmdType
        commandResult.result = data
        return commandResult
    }


    /**
     * 这个方式只执行初级的格式校验，具体的逻辑校验由Validator接口和StringValidator接口执行
     *
     * @param result
     */
    private fun baseValidate(result: String) {
        require(result.isNotEmpty()) { "result为空或者null" }
        require(result.length >= OldIOTCommandResult.RESULT_MIN_LENGTH) { "result长度过短:$result" }
        require(!result.startsWith(OldIOTCommandResult.COMMAND_HEADER)) { "result格式错误:$result" }
    }

    /**
     * 将本包下的Parse注册到parserMap中
     */
    private fun registerParse() {
        val clazzes = listOf(
            DeviceTimeParserOld::class.java,
            DeviceCurrentStateExParserOld::class.java,
            TelemetryParserOld::class.java,
            DataCenterStatusParserOld::class.java,
            DataCenterInfoParserOld::class.java,
            VmsBasicInfoParserOld::class.java,
            VmsAisleTerminalInfoParserOld::class.java,
            VmsAisleInfoParserOld::class.java,
            VmsTerminalSensorInfoParserOld::class.java,
            VmsTerminalCollectorInfoParserOld::class.java,
            VmsTerminalCommInfoParserOld::class.java,
            VmsTerminalSnParserOld::class.java,
            TerminalTelemetryParserOld::class.java,
            AdmeBaseInfoParserOld::class.java,
            AdmeBasicConfigParamParserOld::class.java,
            AdmeMeterWheelParamParserOld::class.java,
            AdmeInclinometerInfoParserOld::class.java,
            AdmeMotionStateParserOld::class.java,
            AdmeStepperMotorInfoParserOld::class.java,
            AdmeExecutiveAgencyInfoParserOld::class.java,
            AdmeMeasuringHoleDepthInfoParserOld::class.java,
            AdmeMotorMotionDistanceInfoParserOld::class.java,
            AdmeGuideGrooveCalibrationInfoParserOld::class.java,
            AdmeMotorMotionAngleInfoParserOld::class.java,
            AdmeCurrentStateInfoParserOld::class.java,
            AdmeWorkModeParserOld::class.java,
            AdmeLowEnergyModeInfoParserOld::class.java,
            AdmeLockedRotorDetectionInfoParserOld::class.java,
            AdmeVoltageConfigParserOld::class.java,
            AdmeAnthropomorphicMovementInfoParserOld::class.java,
            AdmeBrakePadControlInfoParserOld::class.java,
            AdmeMotorPowerInfoParserOld::class.java,
            E40SatelitteInfoParserOld::class.java,
            E40RTKModeInfoParserOld::class.java,
            E40BasePositionInfoParserOld::class.java,
            E40CORSInfoParserOld::class.java,
            E40BoardSolutionInfoParserOld::class.java,
            E40EthernetInfoParserOld::class.java,
            E40SerialPortInfoParserOld::class.java,
            E40GpsWorkInfoParserOld::class.java,
            E40NmeaTimeInfoParserOld::class.java,
            DasCollectorInfoParserOld::class.java,
            DasBaseInfoParserOld::class.java,
            DasNetStatusInfoParserOld::class.java,
            DasSolarStatusInfoParserOld::class.java,
            DasTemperatureAndHumidityStatusinfoParserOld::class.java,
            DasSensorStatusInfoParserOld::class.java,
            DasSubSensorStatusInfoParserOld::class.java,
            DasIOSensorInfoParserOld::class.java,
            DasDigitalPiezometerInfoParserOld::class.java,
            DasDataReportInfoParserOld::class.java,
            DasBdTerminalInfoParserOld::class.java,
            DasExternalSensorInfoParserOld::class.java,
            DasFixedPointReportInfoParserOld::class.java,
            McuAddressParserOld::class.java,
            Rn20BaseInfoParserOld::class.java,
            Rn20ModuleStatusParserOld::class.java,
            Rn20PositionInfoParserOld::class.java,
            LogOutputInfoParserOld::class.java,
            LR200PositionInfoParserOld::class.java,
            AudibleAlarmParserOld::class.java,
            HacMeasuringDataInfoParserOld::class.java,
            HacMeasuringHoleDepthInfoParserOld::class.java,
            HacMotionStateParserOld::class.java,
            HacWarningValueParserOld::class.java,
            HacMotorMotionDistanceInfoParserOld::class.java,
            HacExecutiveAgencyInfoParserOld::class.java,
            WorkModeParser::class.java
        )
        registerWithClass(clazzes)
    }

    private fun registerWithClass(classes: List<Class<*>>) {
        try {
            classes.forEach {
                val resultParser = it.newInstance() as OldIOTResultParser<*>
                if (!parserMap.containsKey(resultParser.commandType())) {
                    parserMap[resultParser.commandType()] = resultParser
                }
            }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }

    companion object {
        private val ourInstance = OldIOTParseManager()
        val instance: OldIOTParseManager
            get() {
                if (ourInstance.parserMap.isEmpty()) {
                    ourInstance.registerParse()
                }
                return ourInstance
            }
    }
}