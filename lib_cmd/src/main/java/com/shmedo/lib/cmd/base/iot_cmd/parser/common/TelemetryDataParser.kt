package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     遥测
 */
@IOTParser
class TelemetryDataParser : IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["datastreams"] ?: ""
    }

    override val commandType: IOTCommandType = IOTCommandType.SAMPLE
}