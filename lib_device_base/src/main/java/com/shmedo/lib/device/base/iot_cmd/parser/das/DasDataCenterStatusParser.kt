package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/2/27
 * 描述： TODO
 */
class DasDataCenterStatusParser : IOTCommandParser<String> {
    override fun parseInstance(keyValueMap: Map<String, String>): String {
        return keyValueMap["status"]!!
    }

    override fun commandType(): IOTCommandType = IOTCommandType.DAS_MD_GET_NET_STATUS
}