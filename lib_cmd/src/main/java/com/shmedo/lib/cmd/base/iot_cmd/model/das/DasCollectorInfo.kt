package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  4/12/21 <br></br>
 * 描述：     DAS 采集器参数
 */
@JsonClass(generateAdapter = true)
data class DasCollectorInfo(
    var type: String = "",//采集器型号
    var addr: String = "",//采集器地址
    var collgap: String = "",//采集间隔
    var calcgap: String = "",//解算间隔
    var standbygap: String = "",//待机时长
    var sensornum: String = "",//接入传感器个数
    var sensitivity: String = "",//灵敏度
)