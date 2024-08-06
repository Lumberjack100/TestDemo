package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1CollectionParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
class MRRS485Port1CollectionParamParser : IOTCommandParser<MRRS485Port1CollectionParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port1CollectionParam {
        return MRRS485Port1CollectionParam().apply {
            noresp = keyValueMap.getOrDefault("noresp", noresp)
            collround = keyValueMap.getOrDefault("collround", collround)
            collfreq = keyValueMap.getOrDefault("collfreq", collfreq)
            collcycle = keyValueMap.getOrDefault("collcycle", collcycle)
            powerontimes = keyValueMap.getOrDefault("powerontimes", powerontimes)
        }
    }
    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT1_COLL
}