package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.iot_cmd.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.parser.CommonSettingCmdResultParser
import com.shmedo.lib.device.base.iot_cmd.parser.DeviceCurrentStateParser
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.parser.WorkModeParser
import com.shmedo.lib.device.base.iot_cmd.parser.m20.M20BaseInfoParser
import org.koin.dsl.module

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

val appModule = module {
    factory { CommonSettingCmdResultParser() }
    factory { WorkModeParser() }
    factory { M20BaseInfoParser() }
    factory { DeviceCurrentStateParser() }

    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<IOTResultParser<*>> = listOf(
            get<CommonSettingCmdResultParser>(),
            get<WorkModeParser>(),
            get<M20BaseInfoParser>(),
            get<DeviceCurrentStateParser>()

        )
        IOTParseManager.getInstance(parsers)
    }
}