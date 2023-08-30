package com.shmedo.lib.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class PgyerApiResponse<T>(
    val code: Int = 0,
    @Json(name = "message")
    val msg: String? = "",
    val data: T? = null
)