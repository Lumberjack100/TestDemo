package com.shmedo.lib.device.base.iot_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.common.AlarmTriggerValueInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
class AlarmTriggerValueInfoParser: IOTCommandParser<AlarmTriggerValueInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AlarmTriggerValueInfo {
        return AlarmTriggerValueInfo().apply {
            level1 = keyValueMap.getOrDefault("level1", level1)
            level2 = keyValueMap.getOrDefault("level2", level2)
            level3 = keyValueMap.getOrDefault("level3", level3)
            level4 = keyValueMap.getOrDefault("level4", level4)
            devlevel1 = keyValueMap.getOrDefault("devlevel1", devlevel1)
            devlevel2 = keyValueMap.getOrDefault("devlevel2", devlevel2)
            devlevel3 = keyValueMap.getOrDefault("devlevel3", devlevel3)
            devlevel4 = keyValueMap.getOrDefault("devlevel4", devlevel4)
        }
    }

override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_ALRAM_TRIGGER_VALUE
}