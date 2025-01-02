package com.shmedo.lib.cmd.base.md_cmd.utils

import com.shmedo.lib.cmd.base.md_cmd.assemble.MDCommandAssemble
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
object MDCommandUtil {
    /**
     * 从指令响应结果中提取指令类型
     *
     * @param result $$224,224297L,0,76FE80356C75ED56EE77044A49E63526
     * @return 指令类型
     */
    fun extractCommandType(result: String): MDCommandType {
        if (result.isBlank() || result.length < MDConstants.RESULT_MIN_LENGTH) {
            return MDCommandType.LENGTH_INVALID
        }

        val cmd = result.substring(0, MDConstants.RESULT_MIN_LENGTH)
            .removePrefix(MDConstants.RESPONSE_COMMAND_HEADER).trim()

        return MDCommandType.entries.firstOrNull { it.toString() == cmd }
            ?: MDCommandType.UNKNOWN_TYPE
    }

    fun getCommand(commandType: MDCommandType): String {
        return MDCommandAssemble<Any>(commandType).toString()
    }

    fun <T> getCommand(commandType: MDCommandType, parameter: T): String {
        return MDCommandAssemble(commandType, parameter).toString()
    }


    /**
     * 字符串倒序
     *
     * @param str 参数字符串
     * @return 倒序后的字符串
     */
    fun reverseString(str: String): String {
        return str.reversed()
    }

    /**
     * 格式化输出2位整数（02d: 0代表前面补充0, 2代表长度为2, d代表参数为正数型）
     *
     * @param str 参数字符串
     * @return 格式化后的字符串
     */
    fun formatStringTwo(str: String?): String {
        if (str.isNullOrEmpty()) {
            return ""
        }
        return String.format(Locale.getDefault(), "%02d", str.toInt())
    }

    /**
     * 格式化输出4位整数（04d: 0代表前面补充0, 4代表长度为4, d代表参数为正数型）
     * 支持处理科学计数法表示的数字字符串
     *
     * @param str 参数字符串，支持普通数字和科学计数法(如: "1000" 或 "1.000000e+03")
     * @return 格式化后的字符串
     */
    fun formatStringFour(str: String?): String {
        if (str.isNullOrEmpty()) {
            return ""
        }
        return try {
            val number = str.toDouble().toInt()
            String.format(Locale.getDefault(), "%04d", number)
        } catch (e: NumberFormatException) {
            ""
        }
    }

    /**
     * 格式化输出5位整数（05d: 0代表前面补充0, 5代表长度为5, d代表参数为正数型）
     * 支持处理科学计数法表示的数字字符串
     *
     * @param str 参数字符串，支持普通数字和科学计数法(如: "1000" 或 "1.000000e+03")
     * @return 格式化后的字符串
     */
    fun formatStringFive(str: String?): String {
        if (str.isNullOrEmpty()) {
            return ""
        }
        return try {
            val number = str.toDouble().toInt()
            String.format(Locale.getDefault(), "%05d", number)
        } catch (e: NumberFormatException) {
            ""
        }
    }
}