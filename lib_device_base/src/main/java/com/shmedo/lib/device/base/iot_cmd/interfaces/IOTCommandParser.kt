package com.shmedo.lib.device.base.iot_cmd.interfaces

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.device.base.iot_cmd.parser.ValidationResult

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

interface IOTCommandParser<T> {
    fun validCheckBeforeParse(result: String): ValidationResult {
        if (result.isBlank())
            return ValidationResult(false, "Result is blank")

        //TODO 其他验证逻辑

        return ValidationResult(true)
    }

    fun parse(result: String): ParseResult<T> {
        return try {
            val keyValueMap = result.split("&").associate { keyValue ->
                keyValue.split("=").let { pair -> pair[0] to pair.getOrElse(1) { "" } }
            }
            // 子类实现
            ParseResult.Success(parseInstance(keyValueMap))
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }

    fun parseInstance(keyValueMap: Map<String, String>): T // 注意这里是抽象方法，需要子类来实现

    fun commandType(): IOTCommandType
}
