package com.shmedo.lib.cmd.base.iot_cmd.parser

import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
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
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.CommonSettingParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DataCenterInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DataCenterStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DataReportTypeParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DeviceCurrentStateParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DeviceCurrentStateParser2
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.DeviceHistorySensorDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.GNSSSateliteInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.LoraCommunicateInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.RadioCommunicateInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.RtkParamInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.SensorInitialParser
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
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasSensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasSolarStatusInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.DasTemperatureAndHumidityStatusInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.das.McuAddressInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.GNSSRawDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.GT600ExStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.ModuleParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.AdmeHacExecutiveAgencyInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMeasuringDataInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMeasuringHoleDepthInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMotionStateParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacMotorMotionDistanceInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.hac.HacWarningValueParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.lr200.LR200ZeroValueParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.DualAntennaDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.GNSSCtlDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.NMEATimeDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.SerialPortDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.Net4GUseDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.EthernetConfigDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.BasePositionDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.MdSensorDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.ElevationMaskDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gt600.GnssModeDataParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.M20BaseInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.M50CorsParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.M50RadioParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m.M50SerialPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRAlarmModuleParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDIPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDOPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDataCenterParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDataCenterStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDataReportTypeParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRDeviceInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRPulsePortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS232Port1ParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS232Port2ParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1CollectionParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port1SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2CollectionParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port2SerialPortParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port3CameraParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port3SensorParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRS485Port3SensorStatusParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRRainGaugeParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRReservoirCapacityParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRScreenParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRWiredNetParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mr.MRWirelessNetParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.UDCORSParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.UDFlowCalculationInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.UD485SerialPortInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.UDRainGaugeSerialPortInfoParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.u_product.ULInitialParamParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.mg301.MG301SatModParser

/**
 * 创建者：gonghe
 * 创建时间：2024/10/30
 * 描述： IOT 解析器注册表
 */

object IOTParserRegistry {
    /**
     * 获取所有解析器实例
     */
    fun getAllParsers(): List<IOTCommandParser<*>> = listOf(
        CommonSettingParser(),
        TelemetryDataParser(),
        TimeCalibrationDataParser(),
        WorkModeParser(),
        DataCenterStatusParser(),
        UpdateLocationInfoParser(),
        DataCenterInfoParser(),
        DeviceHistorySensorDataParser(),

        AdmeBaseInfoParser(),
        AdmeMotionStateParser(),
        AdmeCurrentStateInfoParser(),
        AdmeMeterWheelInfoParser(),
        AdmeInclinometerInfoParser(),
        AdmeStepperMotorInfoParser(),
        AdmeLowEnergyModeInfoParser(),
        AdmeAnthropomorphicMovementInfoParser(),
        AdmeBrakePadControlInfoParser(),
        AdmeMotorPowerInfoParser(),
        AdmeVoltageConfigInfoParser(),
        AdmeLockedRotorDetectionInfoParser(),
        AdmeExecutiveAgencyInfoParser(),
        AdmeBasicConfigInfoParser(),
        AdmeMeasuringHoleDepthInfoParser(),
        AdmeMotorMotionDistanceInfoParser(),
        AdmeGuideGrooveCalibrationInfoParser(),
        AdmeMotorMotionAngleInfoParser(),
        AdmeWorkModeInfoParser(),
        AdmeCalibrationProcessingInfoParser(),
        AdmeAndNegativeTestExceptionHandlingInfoParser(),
        HacMotionStateParser(),
        HacWarningValueParser(),
        AdmeHacExecutiveAgencyInfoParser(),
        HacMeasuringHoleDepthInfoParser(),
        HacMotorMotionDistanceInfoParser(),
        HacMeasuringDataInfoParser(),

        DasBaseInfoParser(),
        DasCollectorInfoParser(),
        DasDataReportInfoParser(),
        DasBdTerminalInfoParser(),
        DasIOSensorInfoParser(),
        DasDigitalPiezometerInfoParserr(),
        DasExternalSensorInfoParser(),
        AudibleAlarmParser(),
        McuAddressInfoParser(),
        DasDataCenterStatusParser(),
        DasSolarStatusInfoParser(),
        DasTemperatureAndHumidityStatusInfoParser(),
        DasSensorStatusParser(),
        DasInternalSensorStatusParser(),
        M20BaseInfoParser(),
        M50SerialPortParamParser(),
        M50CorsParamParser(),
        M50RadioParamParser(),
        DataReportTypeParser(),
        GNSSSateliteInfoParser(),
        DeviceCurrentStateParser(),
        DeviceCurrentStateParser2(),
        MRWirelessNetParser(),
        MRWiredNetParser(),
        MRDataReportTypeParser(),
        MRScreenParamParser(),
        MRDataCenterStatusParser(),
        MRDataCenterParser(),
        MRDeviceInfoParser(),
        MRRS485Port1CollectionParamParser(),
        MRRS485Port1SensorStatusParser(),
        MRRS485Port1SensorParamParser(),
        MRRS485Port2CollectionParamParser(),
        MRRS485Port2SerialPortParamParser(),
        MRRS485Port2SensorStatusParser(),
        MRRS485Port2SensorParamParser(),
        MRRS485Port3SensorStatusParser(),
        MRRS485Port3SensorParamParser(),
        MRRS485Port3CameraParamParser(),
        MRRS232Port1ParamParser(),
        MRRS232Port2ParamParser(),
        MRRainGaugeParamParser(),
        MRPulsePortParamParser(),
        MRDOPortParamParser(),
        MRDIPortParamParser(),
        MRReservoirCapacityParser(),
        MRAlarmModuleParamParser(),
        TerminalIdInfoParser(),
        LoraCommunicateInfoParser(),
        RadioCommunicateInfoParser(),
        RtkParamInfoParser(),
        AlarmSwitchInfoParser(),
        AlarmMonitorPointInfoParser(),
        AlarmTriggerValueInfoParser(),
        AlarmReportIntervalInfoParser(),
        UDCORSParamParser(),
        UDFlowCalculationInfoParser(),
        UD485SerialPortInfoParser(),
        UDRainGaugeSerialPortInfoParser(),
        SensorInitialParser(),
        LR200ZeroValueParser(),
        ULInitialParamParser(),
        GNSSRawDataParser(),
        ModuleParamParser(),
        GT600ExStatusParser(),
        // GT600 GNSS 配置相关解析器
        ElevationMaskDataParser(),
        DualAntennaDataParser(),
        GNSSCtlDataParser(),
        NMEATimeDataParser(),
        // GT600 串口配置解析器
        SerialPortDataParser(),
        MdSensorDataParser(),
        // GT600 网络配置解析器
        Net4GUseDataParser(),
        EthernetConfigDataParser(),
        // GT600 工作模式配置解析器
        BasePositionDataParser(),
        GnssModeDataParser(),
        // MG301 解析器
        MG301SatModParser(),
    )
}
