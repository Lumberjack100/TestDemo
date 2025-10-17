package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2025/4/10
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class M50CorsEntity(
    val sw: String = "0", //开关  0 关闭 1 开启
    val host: String = IOTConstants.NULL_KEY, //域名
    val port: String = IOTConstants.NULL_KEY, //端口
    val username: String = IOTConstants.NULL_KEY, //差分账号
    val password: String = IOTConstants.NULL_KEY, //密码
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}