package com.shmedo.lib.device.base.iot_cmd

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */


sealed class ParseResult<out T> {

    data class Success<out T>(val info: T) : ParseResult<T>()

    data class Failure(val error: String) : ParseResult<Nothing>()
}
