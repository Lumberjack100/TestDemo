package com.shmedo.configlibrary.iot.cmd.parser.das

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.AlarmLevel

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     解析声光报警级别参数
 */
class AlarmLevelParser : IOTResultParser<AlarmLevel?> {
    override fun parse(result: String): AlarmLevel? {
        val info = AlarmLevel()
        return try {
            val keyValues = result.split("&").toTypedArray()
            val keyValueMap = HashMap<String, String>()
            for (keyValue in keyValues) {
                val strs = keyValue.split("=").toTypedArray()
                if (strs.size < 2) {
                    keyValueMap[strs[0]] = ""
                } else {
                    keyValueMap[strs[0]] = strs[1]
                }
            }
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.level1 = keyValueMap.getOrDefault("level1", "NullKey")
            info.level2 = keyValueMap.getOrDefault("level2", "NullKey")
            info.level3 = keyValueMap.getOrDefault("level3", "NullKey")
            info.level4 = keyValueMap.getOrDefault("level4", "NullKey")
            info.level5 = keyValueMap.getOrDefault("level5", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM_LEVEL
    }
}