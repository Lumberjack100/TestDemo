package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/24 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port2SensorParamEntity(
    val chl: String = "",//通道编号 [1,16]
    val model: String = "",//物模型
    val swtoken: String = IOTConstants.NULL_KEY,//水文标识
    val sensortype: String = IOTConstants.NULL_KEY,//传感器类型
    val filtercnt: String = IOTConstants.NULL_KEY,//滤波次数
    val gateval: String = IOTConstants.NULL_KEY,//触发值
    val uplimit: String = IOTConstants.NULL_KEY,//上限值
    val lowlimit: String = IOTConstants.NULL_KEY,//下限值
    val corrvalue: String = IOTConstants.NULL_KEY,//修正值
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}