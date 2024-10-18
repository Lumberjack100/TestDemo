package com.shmedo.lib.cmd.base.iot_cmd.parser

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：    解析器管理器
 */
class IOTParserManager constructor(
    private val parsers: List<IOTCommandParser<*>>
) {
    private val parserMap: Map<IOTCommandType, IOTCommandParser<*>> =
        parsers.associateBy { it.commandType() }

    fun <T> parse(
        resultCmdStr: String,
        cmdType: IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
    ): IOTCommandResult<T> {

        //检查响应指令是否包含表示错误的字段
        if (resultCmdStr.contains(IOTConstants.ERROR_FLAG)) {
            val reason = extractFailureReason(resultCmdStr)
            return IOTCommandResult.Failure(reason, cmdType)
        }

        val parser = parserMap[cmdType] ?: return IOTCommandResult.Failure(
            "未找到命令：$cmdType 的解析器",
            cmdType
        )
        //在调用 parse 进行正式解析前，先对响应指令字符串做个基础检查
        val validationResult = parser.validCheckBeforeParse(resultCmdStr)
        if (!validationResult.isValid)
            return IOTCommandResult.Failure(
                validationResult.errorMessage ?: "指令字符串格式验证失败", cmdType
            )

        return when (val parseResult = parser.parse(resultCmdStr)) {
            is ParseResult.Success -> {
                @Suppress("UNCHECKED_CAST")
                IOTCommandResult.Success(parseResult.data as T, cmdType)
            }

            is ParseResult.Failure -> IOTCommandResult.Failure(parseResult.errorMsg, cmdType)
        }
    }

    /**
     * 从响应指令中提取错误原因
     */
    private fun extractFailureReason(resultCmdStr: String): String {
        val reasonPair = resultCmdStr.split("&").find { it.startsWith("reason=") }
        val reason = reasonPair?.substringAfter("reason=", "未知错误") ?: "未知错误"
        //将 reason 中的"unsupported"转换为可读的中文"设备版本不支持"
        return IOTCommandUtil.convertErrorReason(
            reason,
            IOTCommandUtil.extractCommandType(resultCmdStr)
        )
    }
}
