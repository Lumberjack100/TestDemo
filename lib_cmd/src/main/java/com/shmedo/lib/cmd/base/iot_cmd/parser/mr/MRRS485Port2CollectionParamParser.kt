package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port2CollectionParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser


@IOTParser
class MRRS485Port2CollectionParamParser : IOTCommandParser<MRRS485Port2CollectionParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRS485Port2CollectionParam {
        return MRRS485Port2CollectionParam().apply {
            colladdr = keyValueMap.getOrDefault("colladdr", colladdr)
            colltype = keyValueMap.getOrDefault("colltype", colltype)
            noresp = keyValueMap.getOrDefault("noresp", noresp)
            collround = keyValueMap.getOrDefault("collround", collround)
            collfreq = keyValueMap.getOrDefault("collfreq", collfreq)
            powerontimes = keyValueMap.getOrDefault("powerontimes", powerontimes)
        }
    }
    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_RS485_PORT2_COLL
}