package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40GpsWorkInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/6/18 <br></br>
 * 描述：     解析 GPS 工作参数
 */
class E40GpsWorkInfoParserOld :
    OldIOTResultParser<E40GpsWorkInfo?> {
    override fun parse(result: String): E40GpsWorkInfo? {
        val info = E40GpsWorkInfo()
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
            info.cutoffangle = keyValueMap.getOrDefault("cutoffangle", "NullKey")
            info.range = keyValueMap.getOrDefault("range", "NullKey")
            info.savefreq = keyValueMap.getOrDefault("savefreq", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_GPS_PARAM
    }
}