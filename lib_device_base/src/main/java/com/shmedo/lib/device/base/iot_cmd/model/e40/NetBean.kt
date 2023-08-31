package com.shmedo.lib.device.base.iot_cmd.model.e40

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class NetBean(
    @Json(name = "4g")
    var `_$4g`: String = "",
    var csq: Int = 0,
    var isp: String = "",
    var type: String = "",
    var socket1: Int = 0,
    var socket2: Int = 0,
    var socket3: Int = 0,
    var socket4: Int = 0
    )