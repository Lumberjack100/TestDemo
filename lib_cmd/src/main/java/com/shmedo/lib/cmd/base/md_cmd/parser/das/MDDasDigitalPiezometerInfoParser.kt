package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.MDDasDigitalPiezometerInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/17
 * 描述： TODO
 */
class MDDasDigitalPiezometerInfoParser: MDCommandParser<MDDasDigitalPiezometerInfo> {
    override fun parseInstance(values: List<String>): MDDasDigitalPiezometerInfo {
        return MDDasDigitalPiezometerInfo().apply {
            osmometerStatus = values.getOrNull(1) ?: osmometerStatus
            osmometerAddress = values.getOrNull(2) ?: osmometerAddress
            depthTrigger = values.getOrNull(3) ?: depthTrigger
            depthCorrect = values.getOrNull(4) ?: depthCorrect
            temperatureTrigger = values.getOrNull(5) ?: temperatureTrigger
            temperatureCorrect = values.getOrNull(6) ?: temperatureCorrect
            wireRopeLength = values.getOrNull(7) ?: wireRopeLength
            installElevation = values.getOrNull(8) ?: installElevation
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_OSMOMETER_PARAMETER
}