package com.shmedo.lib.cmd.base.iot_cmd.parser.u_product

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： 北斗林木生长监测终端初始值解析
 */
@IOTParser
class ULInitialParamParser : IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["datastreams"] ?: ""
    }

    override val commandType: IOTCommandType = IOTCommandType.LF_MD_GET_INITIAL_VALUE
}