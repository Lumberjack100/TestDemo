package com.shmedo.lib.cmd.base.iot_cmd.parser.hac

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacWarningValue

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class HacWarningValueParser : IOTCommandParser<HacWarningValue> {
    override fun parseInstance(keyValueMap: Map<String, String>): HacWarningValue {
        return HacWarningValue().apply {
            x1min = keyValueMap.getOrDefault("x1min", x1min)
            x1max = keyValueMap.getOrDefault("x1max", x1max)
            y1min = keyValueMap.getOrDefault("y1min", y1min)
            y1max = keyValueMap.getOrDefault("y1max", y1max)
            x2min = keyValueMap.getOrDefault("x2min", x2min)
            x2max = keyValueMap.getOrDefault("x2max", x2max)
            y2min = keyValueMap.getOrDefault("y2min", y2min)
            y2max = keyValueMap.getOrDefault("y2max", y2max)
            x3min = keyValueMap.getOrDefault("x3min", x3min)
            x3max = keyValueMap.getOrDefault("x3max", x3max)
            y3min = keyValueMap.getOrDefault("y3min", y3min)
            y3max = keyValueMap.getOrDefault("y3max", y3max)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_WARN
}