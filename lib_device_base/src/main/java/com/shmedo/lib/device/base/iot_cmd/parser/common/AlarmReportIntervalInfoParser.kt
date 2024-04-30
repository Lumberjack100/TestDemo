package com.shmedo.lib.device.base.iot_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.common.AlarmReportIntervalInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
class AlarmReportIntervalInfoParser : IOTCommandParser<AlarmReportIntervalInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AlarmReportIntervalInfo {
        return AlarmReportIntervalInfo().apply {
            level1 = keyValueMap.getOrDefault("level1", level1)
            level2 = keyValueMap.getOrDefault("level2", level2)
            level3 = keyValueMap.getOrDefault("level3", level3)
            level4 = keyValueMap.getOrDefault("level4", level4)
            location = keyValueMap.getOrDefault("location", location)
            heartbeat = keyValueMap.getOrDefault("heartbeat", heartbeat)
            collect = keyValueMap.getOrDefault("collect", collect)
            reptgap = keyValueMap.getOrDefault("reptgap", reptgap)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
}