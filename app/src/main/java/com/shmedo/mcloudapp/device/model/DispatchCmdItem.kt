package com.shmedo.mcloudapp.device.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/30 <br></br>
 * 描述：     指令下发/透传接口返回实体类
 */
@JsonClass(generateAdapter = true)
class DispatchCmdItem(
    val deviceToken: String="", //设备SN号
    val msgID: String="" //消息ID,凭借该ID查询该指令的响应结果
)
