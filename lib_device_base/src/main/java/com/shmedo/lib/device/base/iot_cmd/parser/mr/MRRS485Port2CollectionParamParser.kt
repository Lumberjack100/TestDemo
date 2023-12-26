package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port2CollectionParam


class MRRS485Port2CollectionParamParser : IOTCommandParser<MRRS485Port2CollectionParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port2CollectionParam {
        return MRRS485Port2CollectionParam().apply {
            colladdr = keyValueMap.getOrDefault("colladdr", colladdr)
            colltype = keyValueMap.getOrDefault("colltype", colltype)
            noresp = keyValueMap.getOrDefault("noresp", noresp)
            collcycle = keyValueMap.getOrDefault("collcycle", collcycle)
            collfreq = keyValueMap.getOrDefault("collfreq", collfreq)
            powerontimes = keyValueMap.getOrDefault("powerontimes", powerontimes)
        }
    }
    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_COLL
}