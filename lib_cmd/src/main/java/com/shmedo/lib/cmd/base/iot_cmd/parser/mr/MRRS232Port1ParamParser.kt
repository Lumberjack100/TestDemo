package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS232Port1Param
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRS232Port1ParamParser: IOTCommandParser<MRRS232Port1Param> {
        override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRS232Port1Param {
            return MRRS232Port1Param().apply {
                status = keyValueMap.getOrDefault("status", status)
                switch = keyValueMap.getOrDefault("switch", switch)
                type = keyValueMap.getOrDefault("type", type)
                resolut = keyValueMap.getOrDefault("resolut", resolut)
                interval = keyValueMap.getOrDefault("interval", interval)
                workmode = keyValueMap.getOrDefault("workmode", workmode)
                baud = keyValueMap.getOrDefault("baud", baud)
                databit = keyValueMap.getOrDefault("databit", databit)
                parity = keyValueMap.getOrDefault("parity", parity)
                stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
            }
        }

        override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RS232_PORT1_PARAM
}