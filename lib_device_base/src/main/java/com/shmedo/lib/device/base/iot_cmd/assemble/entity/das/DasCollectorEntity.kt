package com.shmedo.lib.device.base.iot_cmd.assemble.entity.das

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/8
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasCollectorEntity(
    var type: String = "",//采集器型号
    var addr: String = IOTConstants.NULL_KEY,//采集器地址
    var collgap: String = IOTConstants.NULL_KEY,//采集间隔
    var calcgap: String = IOTConstants.NULL_KEY,//解算间隔
    var standbygap: String = IOTConstants.NULL_KEY,//待机时长
    var sensornum: String = IOTConstants.NULL_KEY,//接入传感器个数
    var sensitivity: String = IOTConstants.NULL_KEY,//灵敏度
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
