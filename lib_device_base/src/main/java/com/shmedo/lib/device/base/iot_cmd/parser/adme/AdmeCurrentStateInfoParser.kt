package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser

import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCurrentStateInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：      解析 ADME 设备当前状态
 */
class AdmeCurrentStateInfoParser : IOTResultParser<AdmeCurrentStateInfo?> {
    override fun parse(result: String): AdmeCurrentStateInfo? {
        val info = AdmeCurrentStateInfo()
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
            info.sn = keyValueMap.getOrDefault("sn", "NullKey")
            info.productid = keyValueMap.getOrDefault("productid", "NullKey")
            info.simid = keyValueMap.getOrDefault("simid", "NullKey")
            info.imeid = keyValueMap.getOrDefault("imeid", "NullKey")
            info.firversion = keyValueMap.getOrDefault("firversion", "NullKey")
            info.ctrinputv = keyValueMap.getOrDefault("ctrinputv", "NullKey")
            info.driveinputv = keyValueMap.getOrDefault("driveinputv", "NullKey")
            info.inctype = keyValueMap.getOrDefault("inctype", "NullKey")
            info.incnum = keyValueMap.getOrDefault("incnum", "NullKey")
            info.incvoltage = keyValueMap.getOrDefault("incvoltage", "NullKey")
            info.temperature = keyValueMap.getOrDefault("temperature", "NullKey")
            info.humidity = keyValueMap.getOrDefault("humidity", "NullKey")
            info.intertempe = keyValueMap.getOrDefault("intertempe", "NullKey")
            info.signalstr = keyValueMap.getOrDefault("signalstr", "NullKey")
            info.incloc = keyValueMap.getOrDefault("incloc", "NullKey")
            info.abndiasis = keyValueMap.getOrDefault("abndiasis", "NullKey")
            info.downnum = keyValueMap.getOrDefault("downnum", "NullKey")
            info.testway = keyValueMap.getOrDefault("testway", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE
    }
}