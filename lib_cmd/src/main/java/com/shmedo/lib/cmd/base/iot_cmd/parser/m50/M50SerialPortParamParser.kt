package com.shmedo.lib.cmd.base.iot_cmd.parser.m50

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50SerialPortParam

/**
 * 创建者：gonghe
 * 创建时间：2024/9/10
 * 描述： TODO
 */
class M50SerialPortParamParser: IOTCommandParser<M50SerialPortParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): M50SerialPortParam {
        return M50SerialPortParam().apply {
            out_power = keyValueMap.getOrDefault("out_power", out_power)
            rs232_mode = keyValueMap.getOrDefault("rs232_mode", rs232_mode)
            cam_module = keyValueMap.getOrDefault("cam_module", cam_module)
            pixx = keyValueMap.getOrDefault("pixx", pixx)
            pixy = keyValueMap.getOrDefault("pixy", pixy)
            rs485_mode = keyValueMap.getOrDefault("rs485_mode", rs485_mode)
            rs485_baud = keyValueMap.getOrDefault("rs485_baud", rs485_baud)
            rs485_addr = keyValueMap.getOrDefault("rs485_addr", rs485_addr)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.M50_MD_GET_SERIAL_PORT
}