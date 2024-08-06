package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： TODO
 */
class TerminalIdInfoParser : IOTCommandParser<String> {
    override fun parseInstance(keyValueMap: Map<String, String>): String {
        return keyValueMap["id"]!!
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_TERMINAL_ID
}