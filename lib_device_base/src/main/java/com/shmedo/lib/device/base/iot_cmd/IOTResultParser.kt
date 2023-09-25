package com.shmedo.lib.device.base.iot_cmd

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

interface IOTResultParser<T> {
    fun validCheckBeforeParse(result: String): ValidationResult
    fun parse(result: String): ParseResult<T>
    fun commandType(): IOTCommandType
}
