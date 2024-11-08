package com.shmedo.lib.cmd.base.iot_cmd.parser

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.common.CommonSettingParser
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/10/30
 * 描述： IOT 解析器管理器
 * 负责管理所有的解析器并提供统一的解析入口
 */

class IOTParserManager(parsers: List<IOTCommandParser<*>>) {
    private val parserMap: Map<IOTCommandType, IOTCommandParser<*>> =
        parsers.associateBy { it.commandType }
    private val commonSettingParser = CommonSettingParser()

    /**
     * 解析响应数据
     * @param resultCmdStr 响应字符串
     * @param cmdType 命令类型
     * @return 解析结果
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> parse(
        resultCmdStr: String,
        cmdType: IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
    ): IOTCommandResult<T> {

        // 检查响应指令是否包含错误标志
        if (resultCmdStr.contains(IOTConstants.ERROR_FLAG)) {
            val reason = extractFailureReason(resultCmdStr)
            return IOTCommandResult.Failure(IOTCommandUtil.convertErrorReason(reason), cmdType)
        }

        // 获取对应的解析器
        val parser = when {
            cmdType == IOTCommandType.COMMON_SETTING_COMMAND -> commonSettingParser as IOTCommandParser<T>
            else -> parserMap[cmdType] as? IOTCommandParser<T>
                ?: return IOTCommandResult.Failure("未找到指令：$cmdType 的解析器", cmdType)
        }

        return when (val parseResult = parser.parse(resultCmdStr)) {
            is ParseResult.Success -> {
                IOTCommandResult.Success(parseResult.data, cmdType)
            }

            is ParseResult.Failure -> IOTCommandResult.Failure(parseResult.errorMsg, cmdType)
        }
    }

    /**
     * 提取错误原因
     */
    private fun extractFailureReason(resultCmdStr: String): String =
        resultCmdStr.split(IOTConstants.COMMAND_SPLICER)
            .find { it.startsWith("reason=") }
            ?.substringAfter("reason=", "未知错误")
            ?: "未知错误"
}