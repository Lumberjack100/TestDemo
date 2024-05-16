package com.shmedo.lib.device.base.iot_cmd.assemble.entity.hac

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class HacMeasuringHoleDepthInfoEntity(
    val model: String = "", //电机工作标识  0: 重新开始测量 1：继续测量
    val address: String = "", //MAC 地址
    val holeno: String = "", //孔号
    val areano: String = IOTConstants.NULL_KEY,//区号
    val lowtbtss: String = IOTConstants.NULL_KEY, // 下放堵转检测（0:关闭，1:开启）
    val motorspeed: String = IOTConstants.NULL_KEY, // 电机速度
    val measway: String = IOTConstants.NULL_KEY, // 测量模式（0:自动测量，1:手动测量）
    val movementway: String = IOTConstants.NULL_KEY, // /运动方式（0:上拉，1:下放）
    val movedistance: String = IOTConstants.NULL_KEY, // 设定运动距离
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
