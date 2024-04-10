package com.shmedo.lib.device.base.md_cmd.parser

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     物联网指令解析后结果
 */

sealed class MDCommandResult<out T> {

    data class Success<out T>(
        val data: T,
        val commandType: MDCommandType
    ) : MDCommandResult<T>()

    data class Failure(
        val message: String,
        val commandType: MDCommandType? = null
    ) : MDCommandResult<Nothing>()
}
