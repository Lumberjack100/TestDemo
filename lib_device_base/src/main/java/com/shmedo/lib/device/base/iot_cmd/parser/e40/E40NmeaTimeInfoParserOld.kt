package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40NmeaTimeInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/7/4 <br></br>
 * 描述：     TODO
 */
class E40NmeaTimeInfoParserOld :
    OldIOTResultParser<E40NmeaTimeInfo?> {
    override fun parse(result: String): E40NmeaTimeInfo? {
        val info = E40NmeaTimeInfo()
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
            info.gga = keyValueMap.getOrDefault("gga", "NullKey")
            info.rmc = keyValueMap.getOrDefault("rmc", "NullKey")
            info.vtg = keyValueMap.getOrDefault("vtg", "NullKey")
            info.gsv = keyValueMap.getOrDefault("gsv", "NullKey")
            info.gsa = keyValueMap.getOrDefault("gsa", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_NMEA_TIME
    }
}