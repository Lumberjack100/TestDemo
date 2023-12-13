package com.shmedo.mcloudapp.device.model

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
data class CloudDeviceData(
    val timeStr: String = "",
    val dataType: Int,
    val content: String = "",
)
