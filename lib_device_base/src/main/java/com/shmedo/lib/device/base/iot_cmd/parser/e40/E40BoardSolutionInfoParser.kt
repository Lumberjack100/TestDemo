package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40BoardSolutionInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/1/21 <br></br>
 * 描述：    解析板卡解算参数
 */
class E40BoardSolutionInfoParser : IOTResultParser<E40BoardSolutionInfo?> {
    override fun parse(result: String): E40BoardSolutionInfo? {
        val info = E40BoardSolutionInfo()
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
            info.inittime = keyValueMap.getOrDefault("inittime", "NullKey")
            info.calcgap = keyValueMap.getOrDefault("calcgap", "NullKey")
            info.smoothlevel = keyValueMap.getOrDefault("smoothlevel", "NullKey")
            info.reinit = keyValueMap.getOrDefault("reinit", "NullKey")
            info.rtkdynamicmode = keyValueMap.getOrDefault("rtkdynamicmode", "NullKey")
            info.corrval = keyValueMap.getOrDefault("corrval", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_BOARDSOLUTION
    }
}