package com.shmedo.mcloudapp.device.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/18 <br/>
 * 描述：    MR702 串口传感器配置信息
 */
@JsonClass(generateAdapter = true)
data class MR702PortSensorConfig(
    var portName: String = "",
    var sensors: List<SensorModel> = listOf(),
)
@JsonClass(generateAdapter = true)
data class SensorModel(
    val sensorName: String = "",
    val sensorType: String = "",
    val modelToken: String = ""
)