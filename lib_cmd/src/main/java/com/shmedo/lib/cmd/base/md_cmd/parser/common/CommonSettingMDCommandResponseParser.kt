package com.shmedo.lib.cmd.base.md_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/11
 * 描述： TODO
 */
class CommonSettingMDCommandResponseParser: MDCommandParser<String> {

    override fun parse(result: String): ParseResult<String> {
        return try {
            val values = result.replace(MDConstants.COMMAND_FOOTER, "")
            // 子类实现
            ParseResult.Success(values)
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }

    override fun parseInstance(values: List<String>): String {
        TODO("Not yet implemented")
    }


    override fun commandType(): MDCommandType = MDCommandType.COMMON_SETTING_COMMAND
}