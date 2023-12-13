package com.shmedo.lib.network.response

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/12
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class ErrorCode(
    val code: Int = 0,
    val errMessage: String? = "",
)
