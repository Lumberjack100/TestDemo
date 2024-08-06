package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     DAS 状态页面开关量传感器状态
 */
@JsonClass(generateAdapter = true)
data class IoBean(
    var type: String = "",
    var vaule: String = "",
    var errno: String = "",
)