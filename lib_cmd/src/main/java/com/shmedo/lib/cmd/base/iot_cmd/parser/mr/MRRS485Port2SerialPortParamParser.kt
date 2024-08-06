package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRSerialPortParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
class MRRS485Port2SerialPortParamParser : IOTCommandParser<MRSerialPortParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRSerialPortParam {
        return MRSerialPortParam().apply {
            baud = keyValueMap.getOrDefault("baud", baud)
            parity = keyValueMap.getOrDefault("parity", parity)
            databit = keyValueMap.getOrDefault("databit", databit)
            stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_UART
}