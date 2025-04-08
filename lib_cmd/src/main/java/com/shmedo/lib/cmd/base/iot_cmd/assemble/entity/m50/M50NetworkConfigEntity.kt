package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.m50

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2024/8/25 <br/>
 * 描述：     M50网络配置指令参数
 */
@JsonClass(generateAdapter = true)
data class M50NetworkConfigEntity(
    val switch: String = IOTConstants.NULL_KEY,  // 是否开启网络 0:关闭 1:开启
    val networkType: String = IOTConstants.NULL_KEY, // 0:eSIM 1:外置SIM 2:自动
    val apn: String = IOTConstants.NULL_KEY,
    val username: String = IOTConstants.NULL_KEY,
    val password: String = IOTConstants.NULL_KEY
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
} 