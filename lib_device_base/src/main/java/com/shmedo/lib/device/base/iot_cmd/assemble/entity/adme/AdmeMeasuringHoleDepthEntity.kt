package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/2
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AdmeMeasuringHoleDepthEntity(
    var movementway: String = "", //运动方式（0:上拉，1:下放）
    var motorspeed: String = IOTConstants.NULL_KEY, //电机速度
    var movedistance: String = IOTConstants.NULL_KEY, //运动距离
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
