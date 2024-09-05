package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： 一体化雷达泥位计报警上报模式
 */
@JsonClass(generateAdapter = true)
data class UDAlarmReportModeEntity(
    val rept_mode: String = IOTConstants.NULL_KEY, //上报模式 0：自动  1：手动
    val warning_switch: String = IOTConstants.NULL_KEY, //四级报警启用 0：不启用；1：启用
    val ld_reptgap: String = IOTConstants.NULL_KEY, //雷达数据上报频率 分钟
    val angle_threshol: String = IOTConstants.NULL_KEY, //角度偏移阈值
    val pixx: String = IOTConstants.NULL_KEY, //图片水平分辨率
    val pixy: String = IOTConstants.NULL_KEY, //图片垂直分辨率
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
