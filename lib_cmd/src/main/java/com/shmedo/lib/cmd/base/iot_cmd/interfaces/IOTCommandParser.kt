package com.shmedo.lib.cmd.base.iot_cmd.interfaces

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.ValidationResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/10/30
 * 描述：  IOT 指令解析器接口
 */

interface IOTCommandParser<T : Any> {

    /**
     * 解析前的验证
     * @param result 待验证的字符串
     * @return 验证结果
     */
    fun validate(result: String): ValidationResult =
        when {
            result.isBlank() -> ValidationResult(false, "Result is blank")
            !result.startsWith(IOTConstants.COMMAND_HEADER) ->
                ValidationResult(false, "无效的物联网指令格式：缺少指令头部信息。")

            result.length < IOTConstants.RESULT_MIN_LENGTH ->
                ValidationResult(false, "无效的物联网指令格式：指令太短。")

            else -> ValidationResult(true)
        }

    /**
     * 解析响应数据
     * @param result 响应字符串
     * @return 解析结果
     */
    fun parse(result: String): ParseResult<T> {
        return try {
            // 验证
            val validation = validate(result)
            if (!validation.isValid) {
                return ParseResult.Failure(validation.errorMessage ?: "指令字符串格式验证失败")
            }

            // 解析为键值对
            val keyValueMap = result.split(IOTConstants.COMMAND_SPLICER)
                .filter { it.isNotBlank() }
                .associate { keyValue ->
                    keyValue.split("=").let { pair ->
                        pair[0] to if (pair.size == 1) ""
                        else keyValue.substring(pair[0].length + 1)
                    }
                }

            // 子类实现
            ParseResult.Success(parseKeyValueMap(keyValueMap))
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "未知错误"}")
        }
    }

    /**
     * 解析键值对
     * @param keyValueMap 键值对Map
     * @return 解析后的数据对象
     */
    fun parseKeyValueMap(keyValueMap: Map<String, String>): T

    /** 指令类型 */
    val commandType: IOTCommandType
}