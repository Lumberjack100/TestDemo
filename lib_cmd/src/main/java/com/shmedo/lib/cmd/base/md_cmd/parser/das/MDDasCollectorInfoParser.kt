package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/8
 * 描述： TODO
 */
class MDDasCollectorInfoParser : MDCommandParser<DasCollectorInfo> {
    override fun parseInstance(values: List<String>): DasCollectorInfo {
        return DasCollectorInfo().apply {
            type = values.getOrNull(0)?.substring(5) ?: type
            addr = values.getOrNull(1) ?: addr
            standbygap = values.getOrNull(2) ?: standbygap
            calcgap = values.getOrNull(3) ?: calcgap
            collgap = values.getOrNull(4) ?: collgap
            sensornum = values.getOrNull(5) ?: sensornum
            sensitivity = values.getOrNull(6) ?: IOTConstants.NULL_KEY
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.COLLECTOR_CONFIG

}