package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/21
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AdmeBasicConfigEntity(
    var inctype: String = "", //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    var address: String = "", //采集器 / MAC 地址
    var interdeep: String = "", //测斜管孔深
    var downspeed : String = "",//下放速度
    var downwaitetime : String = "",//下放等待时间
    var datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
