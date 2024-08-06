package com.shmedo.lib.device.base.iot_cmd.assemble.entity.common

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AlarmReportIntervalEntity(
    val level1: String = IOTConstants.NULL_KEY,//一级报警上报间隔  默认60,单位s
    val level2: String = IOTConstants.NULL_KEY,//二级报警上报间隔 默认300,单位s
    val level3: String = IOTConstants.NULL_KEY,//三级报警上报间隔 默认1800,单位s
    val level4: String = IOTConstants.NULL_KEY,//四级报警上报间隔 默认3600,单位s
    val location: String = IOTConstants.NULL_KEY,//位置信息上报间隔  [不限]默认7200,单位s
    val heartbeat: String = IOTConstants.NULL_KEY,//心跳包上报间隔  [1-36000]默认60，单位s
    val collect: String = IOTConstants.NULL_KEY,//采集间隔  [1-7200]默认30，单位s
    val reptgap: String = IOTConstants.NULL_KEY,//正常上报周期  [1-36000]默认120，单位min
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
