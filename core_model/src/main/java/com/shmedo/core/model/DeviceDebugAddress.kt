package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/28
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DeviceDebugAddress(
    val deviceServerInfo: DeviceServerInfoInfo,
    val clientServerInfo: DeviceServerInfoInfo,
    val deviceSn: String = "",
    val lastActiveDateTime: String = "",
)

@JsonClass(generateAdapter = true)
data class DeviceServerInfoInfo(
    val serverAddr: String = "",
    val serverPort: Int = 0,
)