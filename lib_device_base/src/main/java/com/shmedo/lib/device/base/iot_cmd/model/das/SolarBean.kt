package com.shmedo.lib.device.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     DAS 状态页面太阳能控制器状态
 */
@JsonClass(generateAdapter = true)
data class SolarBean(
    var errno: Int = 0, //错误码
    var solarvolt: Float = 0f, //太阳能板电压
    var batvolt: Float = 0f, //蓄电池电压
    var solarpwr: Float = 0f, //太阳能板功率
    var loadpwr: Float = 0f, //负载功率
)