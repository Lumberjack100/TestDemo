package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/2/21 <br></br>
 * 描述：      解析ADME 电机运动堵转缓停参数
 */
class AdmeLockedRotorDetectionInfoParser : IOTResultParser<AdmeLockedRotorDetectionInfo?> {
    override fun parse(result: String): AdmeLockedRotorDetectionInfo? {
        val info = AdmeLockedRotorDetectionInfo()
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
            info.lowtbtss = keyValueMap.getOrDefault("lowtbtss", "NullKey")
            info.numpput = keyValueMap.getOrDefault("numpput", "NullKey")
            info.pdajtime = keyValueMap.getOrDefault("pdajtime", "NullKey")
            info.detintiona = keyValueMap.getOrDefault("detintiona", "NullKey")
            info.detintionb = keyValueMap.getOrDefault("detintionb", "NullKey")
            info.lowtorblothr = keyValueMap.getOrDefault("lowtorblothr", "NullKey")
            info.lowtordetime = keyValueMap.getOrDefault("lowtordetime", "NullKey")
            info.lowsusrana = keyValueMap.getOrDefault("lowsusrana", "NullKey")
            info.lowsusranb = keyValueMap.getOrDefault("lowsusranb", "NullKey")
            info.uptbtss = keyValueMap.getOrDefault("uptbtss", "NullKey")
            info.uptorblothr = keyValueMap.getOrDefault("uptorblothr", "NullKey")
            info.uptordetime = keyValueMap.getOrDefault("uptordetime", "NullKey")
            info.upsusrana = keyValueMap.getOrDefault("upsusrana", "NullKey")
            info.upsusranb = keyValueMap.getOrDefault("upsusranb", "NullKey")
            info.holedepth = keyValueMap.getOrDefault("holedepth", "NullKey")
            info.measpacing = keyValueMap.getOrDefault("measpacing", "NullKey")

            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
    }
}