package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeVoltageConfigInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/13 <br></br>
 * 描述：     解析ADME电压配置参数
 */
class AdmeVoltageConfigParser : IOTResultParser<AdmeVoltageConfigInfo?> {
    override fun parse(result: String): AdmeVoltageConfigInfo? {
        val info = AdmeVoltageConfigInfo()
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
            info.volt_power_standard = keyValueMap.getOrDefault("volt_power_standard", "NullKey")
            info.volt_power_low = keyValueMap.getOrDefault("volt_power_low", "NullKey")
            info.volt_power_under = keyValueMap.getOrDefault("volt_power_under", "NullKey")
            info.volt_sensor_standard = keyValueMap.getOrDefault("volt_sensor_standard", "NullKey")
            info.volt_sensor_low = keyValueMap.getOrDefault("volt_sensor_low", "NullKey")
            info.volt_sensor_under = keyValueMap.getOrDefault("volt_sensor_under", "NullKey")
            info.rope_length = keyValueMap.getOrDefault("rope_length", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_VOLTAGE
    }
}