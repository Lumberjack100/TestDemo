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
class MRCollectionParamEntity(
    val colladdr: String = "",//采集器地址
    val colltype: String = "",//采集器类型
    val noresp: String = "",//超时次数/无应答次数
    val collgap: String = "",//采集间隔
    val collfreq: String = "",//采集频率
    val collcycle: String = "",//采集周期
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}