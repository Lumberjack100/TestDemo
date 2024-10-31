package com.shmedo.lib.cmd.base.iot_cmd.parser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     解析结果
 */


/**
 * 解析结果密封类
 * @param T 解析结果的数据类型
 */
sealed class ParseResult<out T> {
    /**
     * 解析成功
     * @property data 解析得到的数据
     */
    data class Success<out T>(val data: T) : ParseResult<T>()

    /**
     * 解析失败
     * @property errorMsg 错误信息
     */
    data class Failure(val errorMsg: String) : ParseResult<Nothing>()
}