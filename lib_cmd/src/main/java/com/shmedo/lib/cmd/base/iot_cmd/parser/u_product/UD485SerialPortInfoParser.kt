package com.shmedo.lib.cmd.base.iot_cmd.parser.u_product

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UD485SerialPortInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/11/7
 * 描述： TODO
 */
@IOTParser
class UD485SerialPortInfoParser : IOTCommandParser<UD485SerialPortInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): UD485SerialPortInfo {
        return UD485SerialPortInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            baud = keyValueMap.getOrDefault("baud", baud)
            addr = keyValueMap.getOrDefault("addr", addr)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.UD_MD_GET_RS485_PARAM
}