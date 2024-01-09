package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasBdTerminalInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
class DasBdTerminalInfoParser: IOTCommandParser<DasBdTerminalInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): DasBdTerminalInfo {
        return DasBdTerminalInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            dstaddr = keyValueMap.getOrDefault("dstaddr", dstaddr)
            baud = keyValueMap.getOrDefault("baud", baud)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.DAS_MD_GET_BD_TERMINAL

}