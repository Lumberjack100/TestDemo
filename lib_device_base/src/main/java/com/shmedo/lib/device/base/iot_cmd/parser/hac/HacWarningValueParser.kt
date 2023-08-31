package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacWarningValue

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/20 <br></br>
 * 描述：      解析 HAC 预警值参数
 */
class HacWarningValueParser : IOTResultParser<HacWarningValue?> {
    override fun parse(result: String): HacWarningValue? {
        val info = HacWarningValue()
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
            info.x1min = keyValueMap.getOrDefault("x1min", "NullKey")
            info.x1max = keyValueMap.getOrDefault("x1max", "NullKey")
            info.x1min = keyValueMap.getOrDefault("y1min", "NullKey")
            info.y1max = keyValueMap.getOrDefault("y1max", "NullKey")
            info.x2min = keyValueMap.getOrDefault("x2min", "NullKey")
            info.x2max = keyValueMap.getOrDefault("x2max", "NullKey")
            info.y3min = keyValueMap.getOrDefault("y2min", "NullKey")
            info.y2max = keyValueMap.getOrDefault("y2max", "NullKey")
            info.x3min = keyValueMap.getOrDefault("x3min", "NullKey")
            info.x3max = keyValueMap.getOrDefault("x3max", "NullKey")
            info.y3min = keyValueMap.getOrDefault("y3min", "NullKey")
            info.y3max = keyValueMap.getOrDefault("y3max", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_WARN
    }
}