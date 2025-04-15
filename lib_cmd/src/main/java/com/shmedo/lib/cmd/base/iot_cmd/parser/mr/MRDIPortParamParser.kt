package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDIPortParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRDIPortParamParser : IOTCommandParser<MRDIPortParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRDIPortParam {
        return MRDIPortParam().apply {
            dstatus1 = keyValueMap.getOrDefault("dstatus1", dstatus1)
            dstatus2 = keyValueMap.getOrDefault("dstatus2", dstatus2)
            dstatus3 = keyValueMap.getOrDefault("dstatus3", dstatus3)
            dstatus4 = keyValueMap.getOrDefault("dstatus4", dstatus4)
            dstatus5 = keyValueMap.getOrDefault("dstatus5", dstatus5)
            dstatus6 = keyValueMap.getOrDefault("dstatus6", dstatus6)
            dstatus7 = keyValueMap.getOrDefault("dstatus7", dstatus7)
            dstatus8 = keyValueMap.getOrDefault("dstatus8", dstatus8)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_DI_PORT_PARAM
}