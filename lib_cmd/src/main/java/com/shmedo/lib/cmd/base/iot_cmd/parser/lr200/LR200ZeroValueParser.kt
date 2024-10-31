package com.shmedo.lib.cmd.base.iot_cmd.parser.lr200

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/10
 * 描述： 米度一体式裂缝计零位校准
 */
@IOTParser
class LR200ZeroValueParser : IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["datastreams"]!!
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_LF_ZERO_VALUE
}