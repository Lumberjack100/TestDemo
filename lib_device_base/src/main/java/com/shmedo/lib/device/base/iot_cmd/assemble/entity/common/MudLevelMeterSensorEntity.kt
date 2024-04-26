package com.shmedo.lib.device.base.iot_cmd.assemble.entity.common

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class MudLevelMeterSensorEntity(
    val height: String = "", //安装高度
    val gap: String = IOTConstants.NULL_KEY, //测量间隔  雷达测量间隔时间(ms)
    val times: String = IOTConstants.NULL_KEY, //平均次数  数据平均次数
    val level: String = IOTConstants.NULL_KEY, //能够触发拍照的级别
    val pixx: String = IOTConstants.NULL_KEY, //图片水平分辨率
    val pixy: String = IOTConstants.NULL_KEY, //图片垂直分辨率
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
