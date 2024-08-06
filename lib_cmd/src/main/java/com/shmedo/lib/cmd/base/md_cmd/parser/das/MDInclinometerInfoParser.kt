package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.InclinometerInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述：
 * $$046,224297L,0,0.144,0.983,-89.013,2.708,-17.306,-1000.931
 */
class MDInclinometerInfoParser : MDCommandParser<InclinometerInfo> {
    override fun parseInstance(values: List<String>): InclinometerInfo {
        return InclinometerInfo(
            status = values.getOrNull(2) ?: "",
            xAxis = values.getOrNull(3) ?: IOTConstants.NULL_KEY,
            yAxis = values.getOrNull(4) ?: IOTConstants.NULL_KEY,
            zAxis = values.getOrNull(5) ?: IOTConstants.NULL_KEY,
            xAcceleration = values.getOrNull(6) ?: IOTConstants.NULL_KEY,
            yAcceleration = values.getOrNull(7) ?: IOTConstants.NULL_KEY,
            zAcceleration = values.getOrNull(8) ?: IOTConstants.NULL_KEY
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_INCLINOMETER_INFO
}