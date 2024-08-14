package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     DAS 状态页面太阳能控制器状态
 */
@JsonClass(generateAdapter = true)
data class SolarBean(
    var errno: String = "", //错误码
    var solarvolt: String = "", //太阳能板电压
    var batvolt: String = "",//蓄电池电压
    var solarpwr: String = "", //太阳能板功率
    var loadpwr: String = "", //负载功率
)