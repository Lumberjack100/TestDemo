package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.AudibleAlarm
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/2/18
 * 描述： TODO
 */
@IOTParser
class AudibleAlarmParser: IOTCommandParser<AudibleAlarm> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AudibleAlarm {
        return AudibleAlarm().apply {
            alarmstatus = keyValueMap.getOrDefault("alarmstatus", alarmstatus)
            screenstatus = keyValueMap.getOrDefault("screenstatus", screenstatus)
            alarmtype = keyValueMap.getOrDefault("alarmtype", alarmtype)
            alarmaddr = keyValueMap.getOrDefault("alarmaddr", alarmaddr)
            level1 = keyValueMap.getOrDefault("level1", level1)
            level2 = keyValueMap.getOrDefault("level2", level2)
            level3 = keyValueMap.getOrDefault("level3", level3)
            playtime = keyValueMap.getOrDefault("playtime", playtime)
            playgap = keyValueMap.getOrDefault("playgap", playgap)
            volume = keyValueMap.getOrDefault("volume", volume)
            screenaddr = keyValueMap.getOrDefault("screenaddr", screenaddr)
            showtime = keyValueMap.getOrDefault("showtime", showtime)
            showgap = keyValueMap.getOrDefault("showgap", showgap)
            mcuaddr = keyValueMap.getOrDefault("mcuaddr", mcuaddr)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM
}