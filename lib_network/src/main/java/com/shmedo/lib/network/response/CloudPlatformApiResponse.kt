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
data class CloudPlatformApiResponse<T>(
    val success: Boolean = false,
    var errCode: ErrorCode? = null,
    val data: T? = null,
)