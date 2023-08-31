package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBasicConfigInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：     解析ADME基础配置参数
 */
class AdmeBasicConfigParamParser : IOTResultParser<AdmeBasicConfigInfo?> {
    override fun parse(result: String): AdmeBasicConfigInfo? {
        val info = AdmeBasicConfigInfo()
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
            info.inctype = keyValueMap.getOrDefault("inctype", "NullKey")
            info.address = keyValueMap.getOrDefault("address", "NullKey")
            info.interdeep = keyValueMap.getOrDefault("interdeep", "NullKey")
            info.downspeed = keyValueMap.getOrDefault("downspeed", "NullKey")
            info.downwaitetime = keyValueMap.getOrDefault("downwaitetime", "NullKey")
            info.datatype = keyValueMap.getOrDefault("datatype", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_BASIC
    }
}