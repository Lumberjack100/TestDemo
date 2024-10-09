package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：      DAS 状态页面数据链路状态
 */
@JsonClass(generateAdapter = true)
data class DasNetStatusInfo(
    var index: Int = 0, //中心编号
    var errno: Int = 0, //错误码
    var send: Int = 0,//已发送
    var unsend: Int = 0, //未发送
    var rate: Float = 0f,//在线率
)