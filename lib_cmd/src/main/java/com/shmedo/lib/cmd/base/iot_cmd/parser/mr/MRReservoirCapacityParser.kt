package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRReservoirCapacity
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/1/23
 * 描述： TODO
 */
@IOTParser
class MRReservoirCapacityParser : IOTCommandParser<MRReservoirCapacity> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRReservoirCapacity {
        return MRReservoirCapacity().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            count = keyValueMap.getOrDefault("count", count)
            xparam = keyValueMap.getOrDefault("xparam", xparam)
            yparam = keyValueMap.getOrDefault("yparam", yparam)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RESERVOIR_CAPACITY
}