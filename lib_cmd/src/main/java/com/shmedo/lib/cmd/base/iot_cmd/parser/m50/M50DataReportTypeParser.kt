package com.shmedo.lib.cmd.base.iot_cmd.parser.m50

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50DataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/7/21
 * 描述： TODO
 */
@IOTParser
class M50DataReportTypeParser: IOTCommandParser<M50DataReportType>  {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): M50DataReportType {
        return M50DataReportType().apply {
            type = keyValueMap.getOrDefault("type", type)
            timehour = keyValueMap.getOrDefault("timehour", timehour)
            timemin = keyValueMap.getOrDefault("timemin", timemin)
            timegap = keyValueMap.getOrDefault("timegap", timegap)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.M50_MD_GET_DATA_REPORT_TYPE
}