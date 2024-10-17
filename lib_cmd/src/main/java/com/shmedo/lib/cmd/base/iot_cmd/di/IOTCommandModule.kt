package com.shmedo.lib.cmd.base.iot_cmd.di

import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeAndNegativeTestExceptionHandlingInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeAnthropomorphicMovementInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeBaseInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeBasicConfigInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeBrakePadControlInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeCalibrationProcessingInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeCurrentStateInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeExecutiveAgencyInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeGuideGrooveCalibrationInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeInclinometerInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeLockedRotorDetectionInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeLowEnergyModeInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMeasuringHoleDepthInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMeterWheelInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMotionStateParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMotorMotionAngleInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMotorMotionDistanceInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeMotorPowerInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeStepperMotorInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeVoltageConfigInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.adme.AdmeWorkModeInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.AlarmMonitorPointInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.AlarmReportIntervalInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.AlarmSwitchInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.AlarmTriggerValueInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.CommonSettingIOTCommandResponseParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DataCenterInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DataCenterStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DeviceCurrentStateParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DeviceCurrentStateParser2
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.GNSSSateliteInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.LoraCommunicateInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.RadioCommunicateInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.RtkParamInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.TelemetryDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.TerminalIdInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.TimeCalibrationDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.UpdateLocationInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.WorkModeParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.AudibleAlarmParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasBaseInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasBdTerminalInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasCollectorInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasDataCenterStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasDataReportInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasDigitalPiezometerInfoParserr
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasExternalSensorInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasIOSensorInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasInternalSensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasReportInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasSensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasSolarStatusInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasTemperatureAndHumidityStatusInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.McuAddressInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.AdmeHacExecutiveAgencyInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMeasuringDataInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMeasuringHoleDepthInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMotionStateParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMotorMotionDistanceInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacWarningValueParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.lr200.LR200ZeroValueParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.m20.M20BaseInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.m50.M50SerialPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDIPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDOPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDataCenterParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDataCenterStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDeviceInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS232Port1ParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS232Port2ParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1CollectionParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2CollectionParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SerialPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port3SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port3SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRainGaugeParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRReportMethodParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRScreenParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRWiredNetParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRWirelessNetParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.UDCORSParamParser
import org.koin.dsl.module

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

val iotCommandModule = module {
    factory { CommonSettingIOTCommandResponseParser() }
    factory { TelemetryDataParser() }
    factory { TimeCalibrationDataParser() }
    factory { WorkModeParser() }
    factory { DataCenterStatusParser() }
    factory { UpdateLocationInfoParser() }
    factory { DataCenterInfoParser() }
    factory { AdmeBaseInfoParser() }
    factory { AdmeMotionStateParser() }
    factory { AdmeCurrentStateInfoParser() }
    factory { AdmeMeterWheelInfoParser() }
    factory { AdmeInclinometerInfoParser() }
    factory { AdmeStepperMotorInfoParser() }
    factory { AdmeLowEnergyModeInfoParser() }
    factory { AdmeAnthropomorphicMovementInfoParser() }
    factory { AdmeBrakePadControlInfoParser() }
    factory { AdmeMotorPowerInfoParser() }
    factory { AdmeVoltageConfigInfoParser() }
    factory { AdmeLockedRotorDetectionInfoParser() }
    factory { AdmeExecutiveAgencyInfoParser() }
    factory { AdmeBasicConfigInfoParser() }
    factory { AdmeMeasuringHoleDepthInfoParser() }
    factory { AdmeMotorMotionDistanceInfoParser() }
    factory { AdmeGuideGrooveCalibrationInfoParser() }
    factory { AdmeMotorMotionAngleInfoParser() }
    factory { AdmeWorkModeInfoParser() }
    factory { AdmeCalibrationProcessingInfoParser() }
    factory { AdmeAndNegativeTestExceptionHandlingInfoParser() }
    factory { HacMotionStateParser() }
    factory { HacWarningValueParser() }
    factory { AdmeHacExecutiveAgencyInfoParser() }
    factory { HacMeasuringHoleDepthInfoParser() }
    factory { HacMotorMotionDistanceInfoParser() }
    factory { HacMeasuringDataInfoParser() }

    factory { DasBaseInfoParser() }
    factory { DasCollectorInfoParser() }
    factory { DasReportInfoParser() }
    factory { DasDataReportInfoParser() }
    factory { DasBdTerminalInfoParser() }
    factory { DasIOSensorInfoParser() }
    factory { DasDigitalPiezometerInfoParserr() }
    factory { DasExternalSensorInfoParser() }
    factory { AudibleAlarmParser() }
    factory { McuAddressInfoParser() }
    factory { DasDataCenterStatusParser() }
    factory { DasSolarStatusInfoParser() }
    factory { DasTemperatureAndHumidityStatusInfoParser() }
    factory { DasSensorStatusParser() }
    factory { DasInternalSensorStatusParser() }
    factory { M20BaseInfoParser() }
    factory { M50SerialPortParamParser() }
    factory { GNSSSateliteInfoParser() }
    factory { DeviceCurrentStateParser() }
    factory { DeviceCurrentStateParser2() }
    factory { MRWirelessNetParser() }
    factory { MRWiredNetParser() }
    factory { MRReportMethodParser() }
    factory { MRScreenParamParser() }
    factory { MRDataCenterStatusParser() }
    factory { MRDataCenterParser() }
    factory { MRDeviceInfoParser() }
    factory { MRRS485Port1CollectionParamParser() }
    factory { MRRS485Port1SensorStatusParser() }
    factory { MRRS485Port1SensorParamParser() }
    factory { MRRS485Port2CollectionParamParser() }
    factory { MRRS485Port2SerialPortParamParser() }
    factory { MRRS485Port2SensorStatusParser() }
    factory { MRRS485Port2SensorParamParser() }
    factory { MRRS485Port3SensorStatusParser() }
    factory { MRRS485Port3SensorParamParser() }
    factory { MRRS232Port1ParamParser() }
    factory { MRRS232Port2ParamParser() }
    factory { MRRainGaugeParamParser() }
    factory { MRDOPortParamParser() }
    factory { MRDIPortParamParser() }
    factory { TerminalIdInfoParser() }
    factory { LoraCommunicateInfoParser() }
    factory { RadioCommunicateInfoParser() }
    factory { RtkParamInfoParser() }
    factory { AlarmSwitchInfoParser() }
    factory { AlarmMonitorPointInfoParser() }
    factory { AlarmTriggerValueInfoParser() }
    factory { AlarmReportIntervalInfoParser() }
    factory { UDCORSParamParser() }
    factory { LR200ZeroValueParser() }


    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<IOTCommandParser<*>> = listOf(
            get<CommonSettingIOTCommandResponseParser>(),
            get<TelemetryDataParser>(),
            get<TimeCalibrationDataParser>(),
            get<WorkModeParser>(),
            get<DataCenterStatusParser>(),
            get<UpdateLocationInfoParser>(),
            get<DataCenterInfoParser>(),
            get<AdmeBaseInfoParser>(),
            get<AdmeMotionStateParser>(),
            get<AdmeCurrentStateInfoParser>(),
            get<AdmeMeterWheelInfoParser>(),
            get<AdmeInclinometerInfoParser>(),
            get<AdmeStepperMotorInfoParser>(),
            get<AdmeLowEnergyModeInfoParser>(),
            get<AdmeAnthropomorphicMovementInfoParser>(),
            get<AdmeBrakePadControlInfoParser>(),
            get<AdmeMotorPowerInfoParser>(),
            get<AdmeVoltageConfigInfoParser>(),
            get<AdmeLockedRotorDetectionInfoParser>(),
            get<AdmeExecutiveAgencyInfoParser>(),
            get<AdmeBasicConfigInfoParser>(),
            get<AdmeMeasuringHoleDepthInfoParser>(),
            get<AdmeMotorMotionDistanceInfoParser>(),
            get<AdmeGuideGrooveCalibrationInfoParser>(),
            get<AdmeMotorMotionAngleInfoParser>(),
            get<AdmeWorkModeInfoParser>(),
            get<AdmeCalibrationProcessingInfoParser>(),
            get<AdmeAndNegativeTestExceptionHandlingInfoParser>(),
            get<HacMotionStateParser>(),
            get<HacWarningValueParser>(),
            get<AdmeHacExecutiveAgencyInfoParser>(),
            get<HacMeasuringHoleDepthInfoParser>(),
            get<HacMotorMotionDistanceInfoParser>(),
            get<HacMeasuringDataInfoParser>(),

            get<DasBaseInfoParser>(),
            get<DasCollectorInfoParser>(),
            get<DasReportInfoParser>(),
            get<DasDataReportInfoParser>(),
            get<DasBdTerminalInfoParser>(),
            get<DasIOSensorInfoParser>(),
            get<DasDigitalPiezometerInfoParserr>(),
            get<DasExternalSensorInfoParser>(),
            get<AudibleAlarmParser>(),
            get<McuAddressInfoParser>(),
            get<DasDataCenterStatusParser>(),
            get<DasSolarStatusInfoParser>(),
            get<DasTemperatureAndHumidityStatusInfoParser>(),
            get<DasSensorStatusParser>(),
            get<DasInternalSensorStatusParser>(),
            get<M20BaseInfoParser>(),
            get<M50SerialPortParamParser>(),
            get<GNSSSateliteInfoParser>(),
            get<DeviceCurrentStateParser>(),
            get<DeviceCurrentStateParser2>(),
            get<MRWirelessNetParser>(),
            get<MRWiredNetParser>(),
            get<MRReportMethodParser>(),
            get<MRScreenParamParser>(),
            get<MRDataCenterStatusParser>(),
            get<MRDataCenterParser>(),
            get<MRDeviceInfoParser>(),
            get<MRRS485Port1CollectionParamParser>(),
            get<MRRS485Port1SensorStatusParser>(),
            get<MRRS485Port1SensorParamParser>(),
            get<MRRS485Port2CollectionParamParser>(),
            get<MRRS485Port2SerialPortParamParser>(),
            get<MRRS485Port2SensorStatusParser>(),
            get<MRRS485Port2SensorParamParser>(),
            get<MRRS485Port3SensorStatusParser>(),
            get<MRRS485Port3SensorParamParser>(),
            get<MRRS232Port1ParamParser>(),
            get<MRRS232Port2ParamParser>(),
            get<MRRainGaugeParamParser>(),
            get<MRDOPortParamParser>(),
            get<MRDIPortParamParser>(),
            get<TerminalIdInfoParser>(),
            get<LoraCommunicateInfoParser>(),
            get<RadioCommunicateInfoParser>(),
            get<RtkParamInfoParser>(),
            get<AlarmSwitchInfoParser>(),
            get<AlarmMonitorPointInfoParser>(),
            get<AlarmTriggerValueInfoParser>(),
            get<AlarmReportIntervalInfoParser>(),
            get<UDCORSParamParser>(),
            get<LR200ZeroValueParser>(),
        )
        IOTParserManager(parsers)
    }
}