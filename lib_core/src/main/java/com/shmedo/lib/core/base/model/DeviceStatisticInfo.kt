package com.shmedo.lib.core.base.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/23 <br></br>
 * 描述：     设备在线统计实体
 */
@JsonClass(generateAdapter = true)
data class DeviceStatisticInfo(
    val totalCount: Int = 0,
    val onlineCount: Int = 0,
    val offlineCount: Int = 0,
    val usableCount: Int = 0,
    val unusableCount: Int = 0,
    val onlinePercent: Double = 0.0,
    val unknownCount: Int = 0
)