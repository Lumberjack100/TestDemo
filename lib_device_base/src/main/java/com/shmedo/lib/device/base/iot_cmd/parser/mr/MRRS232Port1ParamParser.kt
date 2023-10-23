package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS232Port1Param

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
class MRRS232Port1ParamParser: IOTCommandParser<MRRS232Port1Param> {
        override fun parseInstance(keyValueMap: Map<String, String>): MRRS232Port1Param {
            return MRRS232Port1Param().apply {
                status = keyValueMap.getOrDefault("status", status)
                switch = keyValueMap.getOrDefault("switch", switch)
                type = keyValueMap.getOrDefault("type", type)
                resolut = keyValueMap.getOrDefault("resolut", resolut)
                interval = keyValueMap.getOrDefault("interval", interval)
                baud = keyValueMap.getOrDefault("baud", baud)
                databit = keyValueMap.getOrDefault("databit", databit)
                paritybit = keyValueMap.getOrDefault("paritybit", paritybit)
                stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
            }
        }

        override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS232_PORT1_PARAM
}