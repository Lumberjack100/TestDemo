package com.shmedo.lib.device.base.iot_cmd.parser.rn20

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.rn20.Rn20ModuleStatus

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/22 <br></br>
 * 描述：     TODO
 */
class Rn20ModuleStatusParser : IOTResultParser<Rn20ModuleStatus?> {
    override fun parse(result: String): Rn20ModuleStatus? {
        val info = Rn20ModuleStatus()
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
            info.flash = keyValueMap.getOrDefault("flash", "NullKey")
            info.ads = keyValueMap.getOrDefault("ads", "NullKey")
            info.ble = keyValueMap.getOrDefault("ble", "NullKey")
            info.lora = keyValueMap.getOrDefault("lora", "NullKey")
            info.vm501 = keyValueMap.getOrDefault("vm501", "NullKey")
            info.adxl362 = keyValueMap.getOrDefault("adxl362", "NullKey")
            info.mmc5883 = keyValueMap.getOrDefault("mmc5883", "NullKey")
            info.scl3300 = keyValueMap.getOrDefault("scl3300", "NullKey")
            info.aht21 = keyValueMap.getOrDefault("aht21", "NullKey")
            info.rtc = keyValueMap.getOrDefault("rtc", "NullKey")
            info.ltc2945 = keyValueMap.getOrDefault("ltc2945", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.RN20_MD_GET_TERMINAL_MODULE_STATUS
    }
}