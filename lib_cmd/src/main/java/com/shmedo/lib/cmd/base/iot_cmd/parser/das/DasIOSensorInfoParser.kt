package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasIOSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/1/30
 * 描述： TODO
 */
@IOTParser
class DasIOSensorInfoParser : IOTCommandParser<DasIOSensorInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasIOSensorInfo {
        return DasIOSensorInfo().apply {
            type = keyValueMap.getOrDefault("type", type)
            value = keyValueMap.getOrDefault("value", value)
            min_time = keyValueMap.getOrDefault("min_time", IOTConstants.NULL_KEY)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
}