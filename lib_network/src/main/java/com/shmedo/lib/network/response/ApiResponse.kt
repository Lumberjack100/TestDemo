package com.shmedo.lib.network.response

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val code: Int = 0,
    val msg: String? = "",
    val data: T? = null
)