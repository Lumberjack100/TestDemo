package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40BasePositionInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/7/2 <br></br>
 * 描述：    解析 E40基站位置信息
 */
class E40BasePositionInfoParserOld :
    OldIOTResultParser<E40BasePositionInfo?> {
    override fun parse(result: String): E40BasePositionInfo? {
        val info = E40BasePositionInfo()
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
            info.mode = keyValueMap.getOrDefault("mode", "NullKey")
            info.lon = keyValueMap.getOrDefault("lon", "NullKey")
            info.lat = keyValueMap.getOrDefault("lat", "NullKey")
            info.alt = keyValueMap.getOrDefault("alt", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_BASE_POSITION
    }
}