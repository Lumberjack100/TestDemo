package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.parser.common.CommonSettingCmdCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.DeviceCurrentStateParser
import com.shmedo.lib.device.base.iot_cmd.parser.common.WorkModeParser
import com.shmedo.lib.device.base.iot_cmd.parser.m20.M20BaseInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MR485Port1CollectionParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MR485Port1SensorStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MR485Port2CollectionParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MR485Port2SensorStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MR485Port2SerialPortParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDataCenterParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDataCenterStatusParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRDeviceInfoParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port3SensorParamParser
import com.shmedo.lib.device.base.iot_cmd.parser.mr.MRRS485Port3SensorStatusParser
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
    factory { WorkModeParser() }
    factory { M20BaseInfoParser() }
    factory { DeviceCurrentStateParser() }
    factory { MRWirelessNetParser() }
    factory { MRWiredNetParser() }
    factory { MRReportMethodParser() }
    factory { MRScreenParamParser() }
    factory { MRDataCenterStatusParser() }
    factory { MRDataCenterParser() }
    factory { MRDeviceInfoParser() }
    factory { MR485Port1CollectionParamParser() }
    factory { MR485Port1SensorStatusParser() }
    factory { MR485Port2CollectionParamParser() }
    factory { MR485Port2SerialPortParamParser() }
    factory { MR485Port2SensorStatusParser() }
    factory { MRRS485Port3SensorStatusParser() }
    factory { MRRS485Port3SensorParamParser() }


    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<IOTCommandParser<*>> = listOf(
            get<CommonSettingCmdCommandResponseParser>(),
            get<WorkModeParser>(),
            get<M20BaseInfoParser>(),
            get<DeviceCurrentStateParser>(),
            get<MRWirelessNetParser>(),
            get<MRWiredNetParser>(),
            get<MRReportMethodParser>(),
            get<MRScreenParamParser>(),
            get<MRDataCenterStatusParser>(),
            get<MRDataCenterParser>(),
            get<MRDeviceInfoParser>(),
            get<MR485Port1CollectionParamParser>(),
            get<MR485Port1SensorStatusParser>(),
            get<MR485Port2CollectionParamParser>(),
            get<MR485Port2SerialPortParamParser>(),
            get<MR485Port2SensorStatusParser>(),
            get<MRRS485Port3SensorStatusParser>(),
            get<MRRS485Port3SensorParamParser>(),
        )
        IOTParserManager.getInstance(parsers)
    }
}