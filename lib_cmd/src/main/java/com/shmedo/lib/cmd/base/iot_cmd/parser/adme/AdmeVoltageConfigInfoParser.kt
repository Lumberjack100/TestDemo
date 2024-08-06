package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeVoltageConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/18
 *
 * 描述： TODO
 *
 *
 */
class AdmeVoltageConfigInfoParser : IOTCommandParser<AdmeVoltageConfigInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeVoltageConfigInfo {
        return AdmeVoltageConfigInfo().apply {
            volt_power_standard =
                keyValueMap.getOrDefault("volt_power_standard", volt_power_standard)
            volt_power_low = keyValueMap.getOrDefault("volt_power_low", volt_power_low)
            volt_power_under = keyValueMap.getOrDefault("volt_power_under", volt_power_under)
            volt_sensor_standard =
                keyValueMap.getOrDefault("volt_sensor_standard", volt_sensor_standard)
            volt_sensor_low = keyValueMap.getOrDefault("volt_sensor_low", volt_sensor_low)
            volt_sensor_under = keyValueMap.getOrDefault("volt_sensor_under", volt_sensor_under)
            rope_length = keyValueMap.getOrDefault("rope_length", rope_length)
            antifdis = keyValueMap.getOrDefault("antifdis", IOTConstants.NULL_KEY)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_VOLTAGE
}