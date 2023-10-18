package com.shmedo.mcloudapp.device.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/18 <br/>
 * 描述：    MR702 串口传感器配置信息
 */
@JsonClass(generateAdapter = true)
data class MR702InterfaceSensorConfig(
    var interfaceName: String = "",
    var models: List<SensorModel> = listOf(),
)
@JsonClass(generateAdapter = true)
data class SensorModel(
    val modelID: Int = 0,
    val modelName: String = "",
    val modelToken: String = ""
)