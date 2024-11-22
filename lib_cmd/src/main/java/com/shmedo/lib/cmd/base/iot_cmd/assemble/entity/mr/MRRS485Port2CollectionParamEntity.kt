package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port2CollectionParamEntity(
    val colladdr: String = "",//采集器地址
    val colltype: String = "",//采集器类型
    val noresp: String = "",//超时次数/无应答次数
    val collround: String = "",//采集次数
    val collfreq: String = "",//采集频率
    val powerontimes: String = "",//延时时间 s
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}