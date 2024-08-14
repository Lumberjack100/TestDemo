package com.shmedo.lib.cmd.base.iot_cmd.model.mr

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：    MR 串口参数
 */
@JsonClass(generateAdapter = true)
data class MRSensorStatus(
    var sensortype: String = "",//传感器类型
    var model: String = "",//物模型
    var sta: String = "",//接入状态
    var chl: String = "",//通道号
)
