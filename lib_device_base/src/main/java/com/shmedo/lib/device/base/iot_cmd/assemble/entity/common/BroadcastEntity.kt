package com.shmedo.lib.device.base.iot_cmd.assemble.entity.common

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/11
 * 描述： 语音播报参数
 */
@JsonClass(generateAdapter = true)
data class BroadcastEntity(
    val b_num: String = "0",//播报遍数
    val b_size: String = "0",//播报内容大小
    val b_content: String = "",//一级报警上报间隔  默认60,单位s
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
