package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AlarmTriggerValueEntity(
    val level1: String = IOTConstants.NULL_KEY,//一级报警倾角阈值  默认40
    val level2: String = IOTConstants.NULL_KEY,//二级报警倾角阈值  默认20
    val level3: String = IOTConstants.NULL_KEY,//三级报警倾角阈值  默认10
    val level4: String = IOTConstants.NULL_KEY,//四级报警倾角阈值  默认5
    val devlevel1: String = IOTConstants.NULL_KEY,//一级报警阈值  默认40     m20S 设备支持这个字段
    val devlevel2: String = IOTConstants.NULL_KEY,//二级报警阈值  默认20     m20S 设备支持这个字段
    val devlevel3: String = IOTConstants.NULL_KEY,//三级报警阈值  默认10     m20S 设备支持这个字段
    val devlevel4: String = IOTConstants.NULL_KEY,//四级报警阈值  默认5      m20S 设备支持这个字段
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
