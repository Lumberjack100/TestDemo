package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.ParseResult
import com.shmedo.lib.device.base.iot_cmd.ValidationResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
class DeviceCurrentStateParser: IOTResultParser<String> {
    override fun validCheckBeforeParse(result: String): ValidationResult {
        if (result.isBlank())
            return ValidationResult(false, "Result is blank")

        //TODO 其他验证逻辑

        return ValidationResult(true)
    }

    override fun parse(result: String): ParseResult<String> {
        return try {
            val keyValueMap = result.split("&").associate { keyValue ->
                keyValue.split("=").let { pair -> pair[0] to pair.getOrElse(1) { "" } }
            }
            val info = keyValueMap["state"]!!
            ParseResult.Success(info)
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.QUERY_DEVICE_STATUS

}