package com.shmedo.lib.device.base.iot_cmd.assemble.entity.hac

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class HacWarningValueEntity(
    val x1min: String = IOTConstants.NULL_KEY, //一级预警 X 轴最小值
    val x1max: String = IOTConstants.NULL_KEY, //一级预警 X 轴最大值
    val y1min: String = IOTConstants.NULL_KEY, //一级预警 Y 轴最小值
    val y1max: String = IOTConstants.NULL_KEY, //一级预警 Y 轴最大值
    val x2min: String = IOTConstants.NULL_KEY, //二级预警 X 轴最小值
    val x2max: String = IOTConstants.NULL_KEY, //二级预警 X 轴最大值
    val y2min: String = IOTConstants.NULL_KEY, //二级预警 Y 轴最小值
    val y2max: String = IOTConstants.NULL_KEY, //二级预警 Y 轴最大值
    val x3min: String = IOTConstants.NULL_KEY, //三级预警 X 轴最小值
    val x3max: String = IOTConstants.NULL_KEY, //三级预警 X 轴最大值
    val y3min: String = IOTConstants.NULL_KEY, //三级预警 Y 轴最小值
    val y3max: String = IOTConstants.NULL_KEY, //三级预警 Y 轴最大值
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
