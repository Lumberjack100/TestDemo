package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.SerialPortData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 串口参数解析器
 *
 * 对应指令: md_getdbguart
 * 应答示例: $cmd=md_getdbguart&type=1&baud=9600
 */
@IOTParser
class SerialPortDataParser : IOTCommandParser<SerialPortData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): SerialPortData {
        return SerialPortData().apply {
            type = keyValueMap.getOrDefault("type", type)
            baud = keyValueMap.getOrDefault("baud", baud)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_DB_GUART
}
