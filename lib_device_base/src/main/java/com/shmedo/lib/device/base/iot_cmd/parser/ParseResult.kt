package com.shmedo.lib.device.base.iot_cmd.parser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     解析结果
 */


sealed class ParseResult<out T> {

    data class Success<out T>(val data: T) : ParseResult<T>()

    data class Failure(val errorMsg: String) : ParseResult<Nothing>()
}
