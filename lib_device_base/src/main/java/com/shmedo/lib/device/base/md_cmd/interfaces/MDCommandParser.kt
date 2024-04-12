package com.shmedo.lib.device.base.md_cmd.interfaces

import com.shmedo.lib.device.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.device.base.iot_cmd.parser.ValidationResult
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

interface MDCommandParser<T> {
    fun validCheckBeforeParse(result: String): ValidationResult {
        if (result.isBlank())
            return ValidationResult(false, "Result is blank")

        //TODO 其他验证逻辑

        return ValidationResult(true)
    }

    fun parse(result: String): ParseResult<T> {
        return try {
            val values = result.replace("\r\n", "").split(MDConstants.COMMAND_SPLICER_COMMA)
            // 子类实现
            ParseResult.Success(parseInstance(values))
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }

    fun parseInstance(values: List<String>): T

    fun commandType(): MDCommandType
}
