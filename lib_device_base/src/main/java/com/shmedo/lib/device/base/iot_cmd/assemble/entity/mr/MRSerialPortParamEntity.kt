package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class MRSerialPortParamEntity(
    val baud: String = "",//波特率
    val parity: String = "",//校验位
    val databit: String = "",//数据位
    val stopbit: String = "",//停止位
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}