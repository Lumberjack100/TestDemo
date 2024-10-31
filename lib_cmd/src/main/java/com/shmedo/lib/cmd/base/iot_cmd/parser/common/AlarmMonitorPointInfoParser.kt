package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
@IOTParser
class AlarmMonitorPointInfoParser: IOTCommandParser<AlarmMonitorPointInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AlarmMonitorPointInfo {
        return AlarmMonitorPointInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            alarm_send_min_gap = keyValueMap.getOrDefault("alarm_send_min_gap", alarm_send_min_gap)
            alarm_resend_cnt = keyValueMap.getOrDefault("alarm_resend_cnt", alarm_resend_cnt)
            alarm_resend_gap = keyValueMap.getOrDefault("alarm_resend_gap", alarm_resend_gap)
            monitorpoint = keyValueMap.getOrDefault("monitorpoint", monitorpoint)
            cnt = keyValueMap.getOrDefault("cnt", cnt)
            level1 = keyValueMap.getOrDefault("level1", level1)
            level2 = keyValueMap.getOrDefault("level2", level2)
            level3 = keyValueMap.getOrDefault("level3", level3)
            level4 = keyValueMap.getOrDefault("level4", level4)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
}