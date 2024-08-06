package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/2
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AdmeAutoMeasuringHoleDepthEntity(
    val motorspeed: String = "", //电机下放速度
    val safedistance: String = IOTConstants.NULL_KEY, //安全距离补偿 )
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
