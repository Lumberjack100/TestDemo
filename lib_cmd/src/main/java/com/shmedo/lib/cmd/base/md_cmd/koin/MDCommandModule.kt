package com.shmedo.lib.cmd.base.md_cmd.koin

import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.parser.common.CommonSettingMDCommandResponseParser
import com.shmedo.lib.cmd.base.md_cmd.parser.common.MDBleDataCenterInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.common.MDDeviceNetStatusParser
import com.shmedo.lib.cmd.base.md_cmd.parser.common.MDLocalTimeParser
import com.shmedo.lib.cmd.base.md_cmd.parser.common.MDServerAddressInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDAuthenticationInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDAuthenticationResultInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDBreakAlarmStatusInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDasBaseConfigInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDasCollectorInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDasDataReportInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDasDigitalPiezometerInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDasExternalSensorInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDeviceStatusInfoOneParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDeviceStatusInfoThreeParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDDeviceStatusInfoTwoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDInclinometerInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDSystemRunStateInfoParser
import com.shmedo.lib.cmd.base.md_cmd.parser.das.MDVersionMessageInfoParser
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
val mdCommandModule = module {
    singleOf(::CommonSettingMDCommandResponseParser)
    singleOf(::MDLocalTimeParser)
    singleOf(::MDDeviceNetStatusParser)
    singleOf(::MDBleDataCenterInfoParser)
    singleOf(::MDServerAddressInfoParser)
    singleOf(::MDAuthenticationInfoParser)
    singleOf(::MDAuthenticationResultInfoParser)
    singleOf(::MDDasBaseConfigInfoParser)
    singleOf(::MDDasCollectorInfoParser)
    singleOf(::MDDeviceStatusInfoOneParser)
    singleOf(::MDVersionMessageInfoParser)
    singleOf(::MDSystemRunStateInfoParser)
    singleOf(::MDDeviceStatusInfoTwoParser)
    singleOf(::MDInclinometerInfoParser)
    singleOf(::MDDeviceStatusInfoThreeParser)
    singleOf(::MDBreakAlarmStatusInfoParser)
    singleOf(::MDDasDigitalPiezometerInfoParser)
    singleOf(::MDDasExternalSensorInfoParser)
    singleOf(::MDDasDataReportInfoParser)

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
            get<MDDeviceStatusInfoThreeParser>(),
            get<MDBreakAlarmStatusInfoParser>(),
            get<MDDasDigitalPiezometerInfoParser>(),
            get<MDDasExternalSensorInfoParser>(),
            get<MDDasDataReportInfoParser>()
        )
        MDParserManager(parsers)
    }
}