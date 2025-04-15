package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRainGaugeParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRainGaugeParamParser: IOTCommandParser<MRRainGaugeParam> {
        override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRainGaugeParam {
            return MRRainGaugeParam().apply {
                status = keyValueMap.getOrDefault("status", status)
                switch = keyValueMap.getOrDefault("switch", switch)
                rainaccuracy = keyValueMap.getOrDefault("rainaccuracy", rainaccuracy)
                rainelim = keyValueMap.getOrDefault("rainelim", rainelim)
            }
        }

        override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_RAIN_GAUGE_PORT_PARAM
}