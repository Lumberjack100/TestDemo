package com.shmedo.lib.cmd.base.iot_cmd.parser

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     物联网指令解析后结果
 */

sealed class IOTCommandResult<out T> {

    data class Success<out T>(
        val data: T,
        val commandType: IOTCommandType
    ) : IOTCommandResult<T>()

    data class Failure(
        val message: String,
        val commandType: IOTCommandType? = null
    ) : IOTCommandResult<Nothing>()
}
