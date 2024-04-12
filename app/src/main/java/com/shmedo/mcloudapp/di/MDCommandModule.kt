package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.parser.common.BleDataCenterInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.common.CommonSettingMDCommandResponseParser
import com.shmedo.lib.device.base.md_cmd.parser.common.DeviceNetStatusParser
import com.shmedo.lib.device.base.md_cmd.parser.common.MDLocalTimeParser
import com.shmedo.lib.device.base.md_cmd.parser.common.ServerAddressInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.AuthenticationInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.AuthenticationResultInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDasBaseConfigInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDasCollectorInfoParser
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
val mdCommandModule = module {
    factory { CommonSettingMDCommandResponseParser() }
    factory { MDLocalTimeParser() }
    factory { DeviceNetStatusParser() }
    factory { BleDataCenterInfoParser() }
    factory { ServerAddressInfoParser() }
    factory { AuthenticationInfoParser() }
    factory { AuthenticationResultInfoParser() }
    factory { MDDasBaseConfigInfoParser() }
    factory { MDDasCollectorInfoParser() }

    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<MDCommandParser<*>> = listOf(
            get<CommonSettingMDCommandResponseParser>(),
            get<MDLocalTimeParser>(),
            get<DeviceNetStatusParser>(),
            get<BleDataCenterInfoParser>(),
            get<ServerAddressInfoParser>(),
            get<AuthenticationInfoParser>(),
            get<AuthenticationResultInfoParser>(),
            get<MDDasBaseConfigInfoParser>(),
            get<MDDasCollectorInfoParser>()
        )
        MDParserManager.getInstance(parsers)
    }
}