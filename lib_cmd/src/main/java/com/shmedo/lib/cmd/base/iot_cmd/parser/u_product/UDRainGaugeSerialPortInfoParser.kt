package com.shmedo.lib.cmd.base.iot_cmd.parser.u_product

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDRainGaugeSerialPortInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/11/7
 * 描述： TODO
 */
@IOTParser
class UDRainGaugeSerialPortInfoParser : IOTCommandParser<UDRainGaugeSerialPortInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): UDRainGaugeSerialPortInfo {
        return UDRainGaugeSerialPortInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            res = keyValueMap.getOrDefault("res", res)
            total_rain = keyValueMap.getOrDefault("total_rain", total_rain)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.UD_MD_GET_RAIN_GAUGE_PARAM
}