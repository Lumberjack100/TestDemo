package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1CollectionParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRS485Port1CollectionParamParser : IOTCommandParser<MRRS485Port1CollectionParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRS485Port1CollectionParam {
        return MRRS485Port1CollectionParam().apply {
            noresp = keyValueMap.getOrDefault("noresp", noresp)
            collround = keyValueMap.getOrDefault("collround", collround)
            collfreq = keyValueMap.getOrDefault("collfreq", collfreq)
            collcycle = keyValueMap.getOrDefault("collcycle", collcycle)
            powerontimes = keyValueMap.getOrDefault("powerontimes", powerontimes)
        }
    }
    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_RS485_PORT1_COLL
}