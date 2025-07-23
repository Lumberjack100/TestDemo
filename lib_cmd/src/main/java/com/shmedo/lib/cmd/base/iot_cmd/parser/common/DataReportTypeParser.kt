package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/7/21
 * 描述： TODO
 */
@IOTParser
class DataReportTypeParser: IOTCommandParser<DataReportType> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DataReportType {
        return DataReportType().apply {
            type = keyValueMap.getOrDefault("type", type)
            timepoint= keyValueMap.getOrDefault("timepoint", timepoint)
            timehour = keyValueMap.getOrDefault("timehour", timehour)
            timemin = keyValueMap.getOrDefault("timemin", timemin)
            timegap = keyValueMap.getOrDefault("timegap", timegap)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_DATA_REPORT_TYPE
}