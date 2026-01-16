package com.shmedo.lib.cmd.base.iot_cmd.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/1/8
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AttachDataItemBean (
    val key: String = "",
    val value: Any = ""
)