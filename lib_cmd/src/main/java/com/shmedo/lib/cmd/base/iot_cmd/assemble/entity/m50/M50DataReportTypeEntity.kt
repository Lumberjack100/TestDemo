package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.m50

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class M50DataReportTypeEntity(
    var type: String = "",//上报方式 0 固定间隔上报  1 定时定点上报
    var timehour: String = IOTConstants.NULL_KEY,//上报起始时间（小时）  数字,   0-23点（小时）  固定间隔上报下需不需要一个起始时间
    var timemin: String = IOTConstants.NULL_KEY,//上报起始时间（分钟）  数字,   5-60  固定间隔上报下需不需要一个起始时间
    var timegap: String = IOTConstants.NULL_KEY,//上报间隔  分钟,数字    5-1440
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}