package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasDataReportInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/28
 * 描述： TODO
 */
class DasDataReportInfoParser: IOTCommandParser<DasDataReportInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): DasDataReportInfo {
        return DasDataReportInfo().apply {
            report_intv = keyValueMap.getOrDefault("report_intv", report_intv)
            plus_intv = keyValueMap.getOrDefault("plus_intv", plus_intv)
            plus_count = keyValueMap.getOrDefault("plus_count", plus_count)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_DATA_REPORT_TIME
}