package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: GNSS 原始配置实体类，用于设置采样率
 */
@JsonClass(generateAdapter = true)
data class GNSSRawConfigEntity(
    val obs: String = IOTConstants.NULL_KEY // 采样率参数
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
} 