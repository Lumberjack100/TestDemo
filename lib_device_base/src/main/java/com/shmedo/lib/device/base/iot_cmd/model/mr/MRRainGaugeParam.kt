package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     雨量计参数
 */
data class MRRainGaugeParam(
    var status: String = "",//状态  1 已接入 0未接入
    var switch: String = "",//开关  1 开 0关
    var rainaccuracy: String = "",//雨量精度(分辨率)   数字  保留一位有效位
    var rainelim: String = "",//消抖系数  (秒/次)  数字
)
