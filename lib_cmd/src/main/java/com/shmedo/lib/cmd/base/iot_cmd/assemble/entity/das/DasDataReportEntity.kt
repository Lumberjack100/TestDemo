package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/28
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasDataReportEntity(
    val report_intv: String = "", //数据上报间隔
    val plus_intv: String = IOTConstants.NULL_KEY, //加报间隔
    val plus_count: String = IOTConstants.NULL_KEY, //加报次数
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
