package com.shmedo.lib.device.base.iot_cmd.model.das

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：     DAS 状态页面主传感器状态
 */
@JsonClass(generateAdapter = true)
class DasSensorStatusInfo(
    var type: String = "", //传感器类型
    var addr: Int = 0, //传感器地址
    var errno: Int = 0, //错误码
    @Json(name = "val")
    var _val: String = "" //传感器数据
)