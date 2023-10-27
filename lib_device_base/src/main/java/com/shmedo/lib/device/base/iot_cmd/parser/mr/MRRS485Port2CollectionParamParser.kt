package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCollectionParam


class MRRS485Port2CollectionParamParser : IOTCommandParser<MRCollectionParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRCollectionParam {
        return MRCollectionParam().apply {
            colladdr = keyValueMap.getOrDefault("colladdr", colladdr)
            colltype = keyValueMap.getOrDefault("colltype", colltype)
            noresp = keyValueMap.getOrDefault("noresp", noresp)
            collgap = keyValueMap.getOrDefault("collgap", collgap)
            collfreq = keyValueMap.getOrDefault("collfreq", collfreq)
            collcycle = keyValueMap.getOrDefault("collcycle", collcycle)
        }
    }
    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_COLL
}