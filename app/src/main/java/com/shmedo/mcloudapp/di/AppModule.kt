package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.parser.CommonSettingCmdCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.parser.DeviceCurrentStateParser
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.parser.WorkModeParser
import com.shmedo.lib.device.base.iot_cmd.parser.m20.M20BaseInfoParser
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

    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<IOTCommandResponseParser<*>> = listOf(
            get<CommonSettingCmdCommandResponseParser>(),
            get<WorkModeParser>(),
            get<M20BaseInfoParser>(),
            get<DeviceCurrentStateParser>(),
            get<MRWirelessNetParser>(),
            get<MRWiredNetParser>(),
            get<MRReportMethodParser>(),
            get<MRScreenParamParser>()
        )
        IOTParserManager.getInstance(parsers)
    }
}