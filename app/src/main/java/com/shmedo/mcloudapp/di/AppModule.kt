package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeAnthropomorphicMovementInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBaseInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBasicConfigInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeBrakePadControlInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeCurrentStateInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeExecutiveAgencyInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeInclinometerInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeLockedRotorDetectionInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeLowEnergyModeInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMeasuringHoleDepthInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMeterWheelInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotionStateParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotorMotionDistanceInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeMotorPowerInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeStepperMotorInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.adme.AdmeVoltageConfigInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.CommonSettingCmdCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.DeviceCurrentStateParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.TelemetryDataParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.TimeCalibrationDataParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.WorkModeParser
import com.shmedo.lib.device.base.iot_cmd.parser.m20.M20BaseInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDIPortParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDOPortParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDataCenterParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDataCenterStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDeviceInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS232Port1ParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS232Port2ParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port1CollectionParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port1SensorParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port1SensorStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port2CollectionParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port2SensorParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port2SensorStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port2SerialPortParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port3SensorParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port3SensorStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRainGaugeParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRReportMethodParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRScreenParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRWiredNetParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRWirelessNetParser
import org.koin.dsl.module

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

val appModule = module {
    factory { CommonSettingCmdCommandResponseParser() }
    factory { TelemetryDataParser() }
    factory { TimeCalibrationDataParser() }
    factory { WorkModeParser() }
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

    factory { M20BaseInfoParser() }
    factory { DeviceCurrentStateParser() }
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


    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<IOTCommandParser<*>> = listOf(
            get<CommonSettingCmdCommandResponseParser>(),
            get<TelemetryDataParser>(),
            get<TimeCalibrationDataParser>(),
            get<WorkModeParser>(),
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

            get<M20BaseInfoParser>(),
            get<DeviceCurrentStateParser>(),
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
        )
        IOTParserManager.getInstance(parsers)
    }
}