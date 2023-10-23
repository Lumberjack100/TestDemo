package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRainGaugeParamEntity (
    val switch: String = "",//开关  1 开 0关
    val rainaccuracy: String = IOTConstants.NULL_KEY,//雨量精度(分辨率)   数字  保留一位有效位
    val rainelim: String = IOTConstants.NULL_KEY,//消抖系数  (秒/次)  数字
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}