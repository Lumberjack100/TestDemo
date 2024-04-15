package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.InclinometerInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述：
 * $$046,224297L,0,0.144,0.983,-89.013,2.708,-17.306,-1000.931
 */
class MDInclinometerInfoParser: MDCommandParser<InclinometerInfo> {
    override fun parseInstance(values: List<String>): InclinometerInfo {
        return InclinometerInfo(
            status = values.getOrNull(2) ?: "",
            xAxis = values.getOrNull(3) ?: "",
            yAxis = values.getOrNull(4) ?: "",
            zAxis = values.getOrNull(5) ?: "",
            xAcceleration = values.getOrNull(6) ?: "",
            yAcceleration = values.getOrNull(7) ?: "",
            zAcceleration = values.getOrNull(8) ?: ""
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_INCLINOMETER_INFO
}