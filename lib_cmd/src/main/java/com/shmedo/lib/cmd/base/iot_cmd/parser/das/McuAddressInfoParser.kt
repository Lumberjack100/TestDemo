package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.McuAddressInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/2/18
 * 描述： TODO
 */
class McuAddressInfoParser: IOTCommandParser<McuAddressInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): McuAddressInfo {
        return McuAddressInfo().apply {
            mcuaddr = keyValueMap.getOrDefault("mcuaddr", mcuaddr)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.DAS_MD_GET_MCU_ADDRESS

}