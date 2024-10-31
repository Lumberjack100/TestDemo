package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS232Port2Param
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRRS232Port2ParamParser : IOTCommandParser<MRRS232Port2Param> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRS232Port2Param {
        return MRRS232Port2Param().apply {
            status = keyValueMap.getOrDefault("status", status)
            switch = keyValueMap.getOrDefault("switch", switch)
            daddr = keyValueMap.getOrDefault("daddr", daddr)
            baud = keyValueMap.getOrDefault("baud", baud)
            databit = keyValueMap.getOrDefault("databit", databit)
            parity = keyValueMap.getOrDefault("parity", parity)
            stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RS232_PORT2_PARAM
}