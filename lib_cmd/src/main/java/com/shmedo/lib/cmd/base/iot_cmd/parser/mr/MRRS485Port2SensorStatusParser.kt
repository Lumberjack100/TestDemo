package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/17 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRS485Port2SensorStatusParser : IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["status"]!!
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR
}