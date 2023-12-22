package com.shmedo.mcloudapp.device.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/22
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AppConfigContent(
    var mr702: List<MR702PortSensorConfig> = listOf(),
)
