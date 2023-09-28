package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil.extractCommandType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：    解析器管理器
 */
class IOTParserManager private constructor(
    parsers: List<IOTCommandParser<*>>
) {
    private val parserMap: Map<IOTCommandType, IOTCommandParser<*>> =
        parsers.associateBy { it.commandType() }

    fun <T> parse(
        result: String,
        cmdType: IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
    ): IOTCommandResult<T> {

        // 检查是否包含表示失败的字段
        if (result.contains(IOTConstants.ERROR_FLAG)) {
            val reason = extractFailureReason(result)
            val cmdType = extractCommandType(result)
            return IOTCommandResult.Failure(reason, cmdType)
        }

        val parser = parserMap[cmdType] ?: return IOTCommandResult.Failure(
            "未找到命令：$cmdType 的解析器",
            cmdType
        )
        //在调用 parse 进行正式解析前，先对响应指令字符串做个基础检查
        val validationResult = parser.validCheckBeforeParse(result)
        if (!validationResult.isValid) {
            return IOTCommandResult.Failure(validationResult.errorMessage ?: "指令字符串格式验证失败", cmdType)
        }
        return when (val parseResult = parser.parse(result)) {
            is ParseResult.Success -> {
                @Suppress("UNCHECKED_CAST")
                IOTCommandResult.Success(parseResult.info as T, cmdType)
            }

            is ParseResult.Failure -> IOTCommandResult.Failure(parseResult.error, cmdType)
        }
    }

    private fun extractFailureReason(result: String): String {
        val reasonPair = result.split("&").find { it.startsWith("reason=") }
        return reasonPair?.substringAfter("reason=") ?: "未知错误"
    }

    companion object {
        @Volatile
        private var INSTANCE: IOTParserManager? = null

        fun getInstance(parsers: List<IOTCommandParser<*>>): IOTParserManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: IOTParserManager(parsers).also { INSTANCE = it }
            }
    }
}
