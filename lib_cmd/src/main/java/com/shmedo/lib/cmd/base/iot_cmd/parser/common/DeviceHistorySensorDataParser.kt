package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
@IOTParser
class DeviceHistorySensorDataParser: IOTCommandParser<Map<String, String>> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): Map<String, String> {
        return keyValueMap
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_DEVICE_SENSOR_HISTORY_DATA
}