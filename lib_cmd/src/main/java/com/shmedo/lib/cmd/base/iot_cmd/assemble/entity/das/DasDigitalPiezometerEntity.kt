package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/30
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasDigitalPiezometerEntity(
    var sw: String = "", //0：关闭数字水位计采集功能 1：打开数字水位计采集功能
    var addr: String = IOTConstants.NULL_KEY, //地址
    var threshold: String = IOTConstants.NULL_KEY, //触发阈值
    var corrval: String = IOTConstants.NULL_KEY,  //修正值
    var ropelen: String = IOTConstants.NULL_KEY,  //绳长（渗压计到管口的距离）
    var tubealti: String = IOTConstants.NULL_KEY,  //安装高程
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
