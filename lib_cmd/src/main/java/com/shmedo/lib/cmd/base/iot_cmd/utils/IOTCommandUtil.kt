package com.shmedo.lib.cmd.base.iot_cmd.utils

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：    IOT 命令工具类
 */

object IOTCommandUtil {
    /**
     * 从响应结果中提取命令类型
     * @param result 响应结果字符串 "$cmd=reqtime&time=2020-08-31 14:27:21&apikey=80c8b131-80e5-4504-bb67-72e8cf93a542&msgid=63d30270-80f6-4458-bafb-c669585f5e4b"
     * @return 命令类型
     */
    fun extractCommandType(result: String): IOTCommandType {
        if (result.isBlank() || result.length < IOTConstants.RESULT_MIN_LENGTH) {
            return IOTCommandType.LENGTH_INVALID
        }

        val cmd = result.substringBefore(IOTConstants.COMMAND_SPLICER)
            .removePrefix(IOTConstants.COMMAND_HEADER)
            .trim()

        return IOTCommandType.fromString(cmd)
    }

    /**
     * 生成命令字符串
     * @param commandType 命令类型
     * @return 命令字符串
     */
    fun getCommand(commandType: IOTCommandType): String =
        "${IOTConstants.COMMAND_HEADER}${commandType.value}"

    /**
     * 生成带参数的命令字符串
     * @param commandType 命令类型
     * @param parameter 参数
     * @return 命令字符串
     */
    fun <T> getCommand(commandType: IOTCommandType, parameter: T): String =
        "${getCommand(commandType)}${IOTConstants.COMMAND_SPLICER}$parameter"

    /**
     * 将英文错误原因转换为中文
     * @param reason 错误原因
     * @param cmdType 命令类型
     * @return 中文错误说明
     */
    fun convertErrorReason(
        reason: String
    ): String = IOTConstants.errorReasonMap[reason] ?: reason

}
