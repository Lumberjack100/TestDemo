package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRReportMethodEntity (
    val type: String = "1",//上报方式 1 定时定点上报  2 固定间隔上报
    val interval: String = "",//上报间隔  秒,数字
    val basis: String = "",//上报起始时间(基准时间)   数字,   0-23点（小时）  固定间隔上报下需不需要一个起始时间
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}