package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMeterWheelInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/27/20 <br></br>
 * 描述：      解析ADME计米轮配置参数
 */
class AdmeMeterWheelParamParser : IOTResultParser<AdmeMeterWheelInfo?> {
    override fun parse(result: String): AdmeMeterWheelInfo? {
        val info = AdmeMeterWheelInfo()
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
            info.enclinenum = keyValueMap.getOrDefault("enclinenum", "NullKey")
            info.outline = keyValueMap.getOrDefault("outline", "NullKey")
            info.uptiona = keyValueMap.getOrDefault("uptiona", "NullKey")
            info.uptionb = keyValueMap.getOrDefault("uptionb", "NullKey")
            info.upconstant = keyValueMap.getOrDefault("upconstant", "NullKey")
            info.upfilter = keyValueMap.getOrDefault("upfilter", "NullKey")
            info.downtiona = keyValueMap.getOrDefault("downtiona", "NullKey")
            info.downtionb = keyValueMap.getOrDefault("downtionb", "NullKey")
            info.downconstant = keyValueMap.getOrDefault("downconstant", "NullKey")
            info.downfilter = keyValueMap.getOrDefault("downfilter", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_METER_WHEEL
    }
}