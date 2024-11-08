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
data class UDRainGaugeSerialPortEntity(
    val sw: String = IOTConstants.NULL_KEY, //开关   0 关闭 1 启用
    val res: String = IOTConstants.NULL_KEY, //雨量计分辨率  毫米
    val clean_rain: String = IOTConstants.NULL_KEY, //清零累计雨量值   1：清零
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}

