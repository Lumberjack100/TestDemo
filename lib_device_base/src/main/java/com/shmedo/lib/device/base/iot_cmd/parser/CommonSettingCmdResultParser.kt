package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.ParseResult
import com.shmedo.lib.device.base.iot_cmd.ValidationResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.CommonSettingCmdResult

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/9/2 <br></br>
 * 描述：     解析通用的设置指令响应结果
 */
class CommonSettingCmdResultParser : IOTResultParser<CommonSettingCmdResult> {
    override fun validCheckBeforeParse(result: String): ValidationResult {
        if (result.isBlank())
            return ValidationResult(false, "Result is blank")

        //TODO 其他验证逻辑

        return ValidationResult(true)
    }

    override fun parse(result: String): ParseResult<CommonSettingCmdResult> {
        return try {
            val keyValueMap = result.split("&").associate { keyValue ->
                keyValue.split("=").let { pair -> pair[0] to pair.getOrElse(1) { "" } }
            }
            val info = CommonSettingCmdResult().apply {
                isSucceed = keyValueMap["result"].equals("succ")
            }
            ParseResult.Success(info)
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
}