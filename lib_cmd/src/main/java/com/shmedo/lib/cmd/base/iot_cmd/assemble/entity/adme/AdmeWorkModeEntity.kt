package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AdmeWorkModeEntity(
    val workmode: String = "", //工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}

