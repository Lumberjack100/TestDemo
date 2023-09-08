package com.shmedo.lib.device.base.iot_cmd.model.m20

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/19/21 <br></br>
 * 描述：     TODO #gh#
 */
@JsonClass(generateAdapter = true)
data class SensorErrnoBean(
    /**
     * errno : 0
     * sensor_id : 203_1
     */
    val errno: Int = 0,
    val sensor_id: String = ""
)