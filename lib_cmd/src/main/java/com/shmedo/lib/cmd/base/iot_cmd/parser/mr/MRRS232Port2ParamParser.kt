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
            sw = keyValueMap.getOrDefault("sw", sw)
            destaddr = keyValueMap.getOrDefault("destaddr", destaddr)
            baud = keyValueMap.getOrDefault("baud", baud)
            databit = keyValueMap.getOrDefault("databit", databit)
            paritybit = keyValueMap.getOrDefault("paritybit", paritybit)
            stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
            linkid = keyValueMap.getOrDefault("linkid", linkid)
            confirm = keyValueMap.getOrDefault("confirm", confirm)
            codetype = keyValueMap.getOrDefault("codetype", codetype)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_RS232_PORT2_PARAM
}