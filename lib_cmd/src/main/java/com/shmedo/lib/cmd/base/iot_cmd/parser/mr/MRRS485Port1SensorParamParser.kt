package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRS485Port1SensorParamParser : IOTCommandParser<String> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["data"]!!
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM

}