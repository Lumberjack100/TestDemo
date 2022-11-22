package com.shmedo.configlibrary.iot.cmd.parser.rn20

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.rn20.Rn20PositionInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/4 <br></br>
 * 描述：     解析雨量采集器经纬度信息
 */
class Rn20PositionInfoParser : IOTResultParser<Rn20PositionInfo?> {
    override fun parse(result: String): Rn20PositionInfo? {
        val info = Rn20PositionInfo()
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
            info.longitude = keyValueMap.getOrDefault("longitude", "NullKey")
            info.latitude = keyValueMap.getOrDefault("latitude", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.RN20_MD_GET_TERMINAL_LOCAL
    }
}