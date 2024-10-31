package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/1/8
 * 描述： TODO
 */
@IOTParser
class DasCollectorInfoParser: IOTCommandParser<DasCollectorInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasCollectorInfo {
        return DasCollectorInfo().apply {
            type = keyValueMap.getOrDefault("type", type)
            addr = keyValueMap.getOrDefault("addr", addr)
            collgap = keyValueMap.getOrDefault("collgap", collgap)
            calcgap = keyValueMap.getOrDefault("calcgap", calcgap)
            standbygap = keyValueMap.getOrDefault("standbygap", standbygap)
            sensornum = keyValueMap.getOrDefault("sensornum", sensornum)
            sensitivity = keyValueMap.getOrDefault("sensitivity", IOTConstants.NULL_KEY)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL

}