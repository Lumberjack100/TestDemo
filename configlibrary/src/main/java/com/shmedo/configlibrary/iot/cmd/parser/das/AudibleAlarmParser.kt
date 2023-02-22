package com.shmedo.configlibrary.iot.cmd.parser.das

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.AudibleAlarm

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：    解析声光报警参数
 */
class AudibleAlarmParser : IOTResultParser<AudibleAlarm?> {
    override fun parse(result: String): AudibleAlarm ?{
        val info = AudibleAlarm()
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
            info.alarmstatus = keyValueMap.getOrDefault("alarmstatus", "NullKey")
            info.screenstatus = keyValueMap.getOrDefault("screenstatus", "NullKey")
            info.alarmtype = keyValueMap.getOrDefault("alarmtype", "NullKey")
            info.alarmaddr = keyValueMap.getOrDefault("alarmaddr", "NullKey")
            info.level1 = keyValueMap.getOrDefault("level1", "NullKey")
            info.level2 = keyValueMap.getOrDefault("level2", "NullKey")
            info.level3 = keyValueMap.getOrDefault("level3", "NullKey")
            info.playtime = keyValueMap.getOrDefault("playtime", "NullKey")
            info.playgap = keyValueMap.getOrDefault("playgap", "NullKey")
            info.screenaddr = keyValueMap.getOrDefault("screenaddr", "NullKey")
            info.showtime = keyValueMap.getOrDefault("showtime", "NullKey")
            info.showgap = keyValueMap.getOrDefault("showgap", "NullKey")

            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM
    }
}