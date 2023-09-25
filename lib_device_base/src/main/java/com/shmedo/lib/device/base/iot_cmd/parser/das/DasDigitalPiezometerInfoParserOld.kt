package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasDigitalPiezometerInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：      解析DAS 数字水位计参数
 */
class DasDigitalPiezometerInfoParserOld :
    OldIOTResultParser<DasDigitalPiezometerInfo?> {
    override fun parse(result: String): DasDigitalPiezometerInfo? {
        val info = DasDigitalPiezometerInfo()
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
            info.addr = keyValueMap.getOrDefault("addr", "NullKey")
            info.sw = keyValueMap.getOrDefault("sw", "NullKey")
            info.threshold = keyValueMap.getOrDefault("threshold", "NullKey")
            info.corrval = keyValueMap.getOrDefault("corrval", "NullKey")
            info.ropelen = keyValueMap.getOrDefault("ropelen", "NullKey")
            info.tubealti = keyValueMap.getOrDefault("tubealti", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
    }
}