package com.shmedo.mcloudapp.di

import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.parser.common.CommonSettingMDCommandResponseParser
import com.shmedo.lib.device.base.md_cmd.parser.common.MDBleDataCenterInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.common.MDDeviceNetStatusParser
import com.shmedo.lib.device.base.md_cmd.parser.common.MDLocalTimeParser
import com.shmedo.lib.device.base.md_cmd.parser.common.MDServerAddressInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDAuthenticationInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDAuthenticationResultInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDasBaseConfigInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDasCollectorInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDeviceStatusInfoOneParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDeviceStatusInfoThreeParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDDeviceStatusInfoTwoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDInclinometerInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDSystemRunStateInfoParser
import com.shmedo.lib.device.base.md_cmd.parser.das.MDVersionMessageInfoParser
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
val mdCommandModule = module {
    factory { CommonSettingMDCommandResponseParser() }
    factory { MDLocalTimeParser() }
    factory { MDDeviceNetStatusParser() }
    factory { MDBleDataCenterInfoParser() }
    factory { MDServerAddressInfoParser() }
    factory { MDAuthenticationInfoParser() }
    factory { MDAuthenticationResultInfoParser() }
    factory { MDDasBaseConfigInfoParser() }
    factory { MDDasCollectorInfoParser() }
    factory { MDDeviceStatusInfoOneParser() }
    factory { MDVersionMessageInfoParser() }
    factory { MDSystemRunStateInfoParser() }
    factory { MDDeviceStatusInfoTwoParser() }
    factory { MDInclinometerInfoParser() }
    factory { MDDeviceStatusInfoThreeParser() }

    // 提供 IOTParseManager 的实例
    single {
        val parsers: List<MDCommandParser<*>> = listOf(
            get<CommonSettingMDCommandResponseParser>(),
            get<MDLocalTimeParser>(),
            get<MDDeviceNetStatusParser>(),
            get<MDBleDataCenterInfoParser>(),
            get<MDServerAddressInfoParser>(),
            get<MDAuthenticationInfoParser>(),
            get<MDAuthenticationResultInfoParser>(),
            get<MDDasBaseConfigInfoParser>(),
            get<MDDasCollectorInfoParser>(),
            get<MDDeviceStatusInfoOneParser>(),
            get<MDVersionMessageInfoParser>(),
            get<MDSystemRunStateInfoParser>(),
            get<MDDeviceStatusInfoTwoParser>(),
            get<MDInclinometerInfoParser>(),
            get<MDDeviceStatusInfoThreeParser>()
        )
        MDParserManager.getInstance(parsers)
    }
}