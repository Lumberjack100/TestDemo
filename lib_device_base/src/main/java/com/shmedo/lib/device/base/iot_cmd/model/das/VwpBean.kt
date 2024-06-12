package com.shmedo.lib.device.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/2/27
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class VwpBean(
    var type: String = "",
    var vaule: String = "",
    var errno: String = "",
)
