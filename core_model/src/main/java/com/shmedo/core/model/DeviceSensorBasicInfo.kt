package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/9/3
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DeviceSensorBasicInfo(
    val id: String = "", //传感器 ID
    val name: String = "", //传感器名称
    val alias: String = "", //传感器别名
    val iotSensorType: String = "", //传感器类型  "104"
    val iotSensorTypeStr: String = "",//传感器类型描述  "倾角计"
)
