package com.shmedo.lib.device.base.iot_cmd.parser.lr200

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.lr200.LR200PositionInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/2/25 <br></br>
 * 描述：     解析一体式裂缝计经纬度信息
 */
class LR200PositionInfoParserOld :
    OldIOTResultParser<LR200PositionInfo?> {
    override fun parse(result: String): LR200PositionInfo? {
        val info = LR200PositionInfo()
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
            info.lng = keyValueMap.getOrDefault("lng", "NullKey")
            info.lat = keyValueMap.getOrDefault("lat", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.MD_GET_LOCATION
    }
}