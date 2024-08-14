package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDOPortParam
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
class MRDOPortParamParser: IOTCommandParser<MRDOPortParam> {
        override fun parseInstance(keyValueMap: Map<String, String>): MRDOPortParam {
            return MRDOPortParam().apply {
                kstatus1 = keyValueMap.getOrDefault("kstatus1", kstatus1)
                kstatus2 = keyValueMap.getOrDefault("kstatus2", kstatus2)
                kstatus3 = keyValueMap.getOrDefault("kstatus3", kstatus3)
                kstatus4 = keyValueMap.getOrDefault("kstatus4", kstatus4)
                kstatus5 = keyValueMap.getOrDefault("kstatus5", kstatus5)
                kstatus6 = keyValueMap.getOrDefault("kstatus6", kstatus6)
                kstatus7 = keyValueMap.getOrDefault("kstatus7", kstatus7)
                kstatus8 = keyValueMap.getOrDefault("kstatus8", kstatus8)
            }
        }

        override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_DO_PORT_PARAM
}