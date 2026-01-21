package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2025/10/7
 * 描述：M50 电台参数配置实体
 */
@JsonClass(generateAdapter = true)
data class M50RadioParamEntity(
    val method: String = "1",
    val sw: String = IOTConstants.NULL_KEY,
    val freq_group: String = IOTConstants.NULL_KEY,
    val airbaud: String = IOTConstants.NULL_KEY,
    val txpower: String = IOTConstants.NULL_KEY,//发射功率  [0 - 20]
    val local_addr: String = IOTConstants.NULL_KEY,
    val target_addr: String = IOTConstants.NULL_KEY
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
