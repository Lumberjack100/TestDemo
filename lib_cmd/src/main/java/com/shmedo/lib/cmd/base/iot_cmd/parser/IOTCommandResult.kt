package com.shmedo.lib.cmd.base.iot_cmd.parser

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     物联网指令解析后结果
 */

/**
 * IOT 指令执行结果密封类
 * @param T 结果数据类型
 */
sealed class IOTCommandResult<out T> {
    /**
     * 指令执行成功
     * @property data 结果数据
     * @property commandType 指令类型
     */
    data class Success<out T>(
        val data: T,
        val commandType: IOTCommandType
    ) : IOTCommandResult<T>()

    /**
     * 指令执行失败
     * @property message 错误信息
     * @property commandType 指令类型,可能为空
     */
    data class Failure(
        val message: String,
        val commandType: IOTCommandType? = null
    ) : IOTCommandResult<Nothing>()
}
