package com.shmedo.mcloudapp.device.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/30 <br></br>
 * 描述：     查询指令结果返回实体
 */
@JsonClass(generateAdapter = true)
class QueryCmdResult(
    val msgID: String = "",
    val deviceID: Int = 0,
    val deviceSN: String = "",
    val cmdEngName: String = "",
    val cmdChnName: String = "",
    val cmdContent: String = "",
    val dispatchTime: String = "",
    val cmdStatus: Int = 0,
    val cmdStatusString: String = "",
    val responseTime: String = "",
    val responseContent: String = "",
    val responseStatus: Int = 0,
    val responseStatusString: String = "",
    val dispatchUserID: Int = 0,
    val dispatchUserName: String = "",
    val cmdID: String = ""
)
