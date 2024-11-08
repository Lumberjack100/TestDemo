package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/10/30
 * 描述： TODO
 */


/**
 * 通用设置命令结果解析器
 */
@IOTParser
class CommonSettingParser : IOTCommandParser<CommonSettingCmdResult> {
    override val commandType = IOTCommandType.COMMON_SETTING_COMMAND

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): CommonSettingCmdResult {
        val result = keyValueMap["result"] ?: "fail"
        return CommonSettingCmdResult(result == "succ")
    }
}