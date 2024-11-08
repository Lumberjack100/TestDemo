package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/11/8
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class UD485SerialPortEntity(
    val sw: String = IOTConstants.NULL_KEY, //开关   0 关闭 1 启用
    val baud: String = IOTConstants.NULL_KEY, //波特率
    val addr: String = IOTConstants.NULL_KEY, //本机地址
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
