package com.shmedo.mcloudapp.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/23 <br></br>
 * 描述：     查询指令执行结果参数
 */
@JsonClass(generateAdapter = true)
class QueryCmdResultParam(
    val msgIDList: List<String> = arrayListOf() //指令下发后返回的MsgID列表
)
