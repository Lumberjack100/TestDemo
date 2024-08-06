package com.shmedo.lib.cmd.base.iot_cmd.parser.lr200

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/10
 * 描述： 米度一体式裂缝计零位校准
 */
class LR200ZeroValueParser : IOTCommandParser<String> {
    override fun parseInstance(keyValueMap: Map<String, String>): String {
        return keyValueMap["datastreams"]!!
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_LF_ZERO_VALUE
}