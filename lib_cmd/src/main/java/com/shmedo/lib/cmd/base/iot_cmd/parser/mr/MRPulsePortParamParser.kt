package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRPulsePortParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     脉冲端口参数解析器
 */
@IOTParser
class MRPulsePortParamParser: IOTCommandParser<MRPulsePortParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRPulsePortParam {
        return MRPulsePortParam().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            workmode = keyValueMap.getOrDefault("workmode", workmode)
            dryaccuracy = keyValueMap.getOrDefault("dryaccuracy", dryaccuracy)
            dryelim = keyValueMap.getOrDefault("dryelim", dryelim)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_PULSE_PORT_PARAM
} 