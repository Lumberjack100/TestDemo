package com.shmedo.lib.device.base.md_cmd.parser


import com.shmedo.lib.device.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：    解析器管理器
 */
class MDParserManager private constructor(
    parsers: List<MDCommandParser<*>>
) {
    private val parserMap: Map<MDCommandType, MDCommandParser<*>> =
        parsers.associateBy { it.commandType() }

    fun <T> parse(
        result: String,
        cmdType: MDCommandType = MDCommandType.COMMON_SETTING_COMMAND
    ): MDCommandResult<T> {

        val cmdStr = result.replace("\r\n", "")
        //检查响应指令是否包含表示错误的字段
        if (cmdStr.endsWith(MDConstants.ERROR_FLAG)) {
            val reason = extractFailureReason(cmdStr)
            return MDCommandResult.Failure(reason, cmdType)
        }

        val parser = parserMap[cmdType] ?: return MDCommandResult.Failure(
            "未找到命令：$cmdType 的解析器",
            cmdType
        )

        //在调用 parse 进行正式解析前，先对响应指令字符串做个基础检查
        val validationResult = parser.validCheckBeforeParse(cmdStr)
        if (!validationResult.isValid)
            return MDCommandResult.Failure(
                validationResult.errorMessage ?: "指令字符串格式验证失败", cmdType
            )

        return when (val parseResult = parser.parse(cmdStr)) {
            is ParseResult.Success -> {
                @Suppress("UNCHECKED_CAST")
                MDCommandResult.Success(parseResult.data as T, cmdType)
            }

            is ParseResult.Failure -> MDCommandResult.Failure(parseResult.errorMsg, cmdType)
        }
    }

    /**
     * 从响应指令中提取错误原因
     */
    private fun extractFailureReason(result: String): String {
        return "未知错误"
    }

    companion object {
        @Volatile
        private var INSTANCE: MDParserManager? = null

        fun getInstance(parsers: List<MDCommandParser<*>>): MDParserManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MDParserManager(parsers).also { INSTANCE = it }
            }
    }
}
