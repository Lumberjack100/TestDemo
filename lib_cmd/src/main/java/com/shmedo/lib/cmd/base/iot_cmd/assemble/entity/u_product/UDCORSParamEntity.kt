package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： 一体化雷达泥位计CORS参数
 */
@JsonClass(generateAdapter = true)
class UDCORSParamEntity(
    val alt_get_mode: String = IOTConstants.NULL_KEY, //测高模式 0：自动；1：手动
    val host: String = IOTConstants.NULL_KEY, //域名 可为ip或域名
    val port: String = IOTConstants.NULL_KEY, //端口
    val username: String = IOTConstants.NULL_KEY, //差分账号
    val password: String = IOTConstants.NULL_KEY, //密码
    val alt: String = IOTConstants.NULL_KEY, //密码
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}