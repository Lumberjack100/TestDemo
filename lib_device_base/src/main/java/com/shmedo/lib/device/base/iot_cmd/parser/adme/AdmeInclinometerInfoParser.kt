package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeInclinometerInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：    解析ADME测斜仪配置参数
 */
class AdmeInclinometerInfoParser : IOTResultParser<AdmeInclinometerInfo?> {
    override fun parse(result: String): AdmeInclinometerInfo? {
        val info = AdmeInclinometerInfo()
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
            info.lowpower = keyValueMap.getOrDefault("lowpower", "NullKey")
            info.address = keyValueMap.getOrDefault("address", "NullKey")
            info.collinval = keyValueMap.getOrDefault("collinval", "NullKey")
            info.calcinval = keyValueMap.getOrDefault("calcinval", "NullKey")
            info.dormancytime = keyValueMap.getOrDefault("dormancytime", "NullKey")
            info.interupdate = keyValueMap.getOrDefault("interupdate", "NullKey")
            info.mode = keyValueMap.getOrDefault("mode", "NullKey")
            info.incversion = keyValueMap.getOrDefault("incversion", "NullKey")
            info.compenway = keyValueMap.getOrDefault("compenway", "NullKey")
            info.torangle = keyValueMap.getOrDefault("torangle", "NullKey")

            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_INCLINOMETER
    }
}