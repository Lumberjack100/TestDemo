package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.McuAddressInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/2/18
 * 描述： TODO
 */
@IOTParser
class McuAddressInfoParser: IOTCommandParser<McuAddressInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): McuAddressInfo {
        return McuAddressInfo().apply {
            mcuaddr = keyValueMap.getOrDefault("mcuaddr", mcuaddr)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_MCU_ADDRESS

}