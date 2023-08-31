package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40SerialPortInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/18 <br></br>
 * 描述：       解析串口参数
 */
class E40SerialPortInfoParser : IOTResultParser<E40SerialPortInfo?> {
    override fun parse(result: String): E40SerialPortInfo? {
        val info = E40SerialPortInfo()
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
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.baud = keyValueMap.getOrDefault("baud", "NullKey")
            info.databits = keyValueMap.getOrDefault("databits", "NullKey")
            info.parity = keyValueMap.getOrDefault("parity", "NullKey")
            info.stopbits = keyValueMap.getOrDefault("stopbits", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_DB_GUART
    }
}