package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasReportInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
@IOTParser
class DasReportInfoParser : IOTCommandParser<DasReportInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasReportInfo {
        return DasReportInfo().apply {
            type = keyValueMap.getOrDefault("type", type)
            timepoint = keyValueMap.getOrDefault("timepoint", timepoint)
            timegap = keyValueMap.getOrDefault("timegap", timegap)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE
}