package com.shmedo.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class DeviceDetailInfo(
    @Json(name = "deviceBaseInfo")
    val deviceInfo: DeviceInfo
)