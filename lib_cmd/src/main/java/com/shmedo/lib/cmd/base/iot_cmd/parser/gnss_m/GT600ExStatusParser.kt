package com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2026/1/8
 * 描述：
 */
@IOTParser
class GT600ExStatusParser: IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["status"]!!
    }

    override val commandType: IOTCommandType = IOTCommandType.GT600_GET_EXSTATUS
}