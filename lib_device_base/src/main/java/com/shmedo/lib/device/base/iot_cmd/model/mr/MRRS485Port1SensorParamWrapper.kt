package com.shmedo.lib.device.base.iot_cmd.model.mr

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/30 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port1SensorParamWrapper(
    var index: String = "",//
    var indexnum: String = "",//
    val port1_param: List<MRRS485Port1SensorParam> = listOf(),
)
