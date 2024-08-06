package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasBdTerminalEntity(
    var sw: String = "",//0 关闭 1 开启
    var dstaddr: String = IOTConstants.NULL_KEY, //目标地址
    var baud: String = IOTConstants.NULL_KEY //波特率
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
