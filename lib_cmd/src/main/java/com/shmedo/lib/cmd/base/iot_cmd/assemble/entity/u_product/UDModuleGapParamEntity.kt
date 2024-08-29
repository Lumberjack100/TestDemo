package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class UDModuleGapParamEntity(
    val ld_module: String = IOTConstants.NULL_KEY, //雷达采集频率 60S的整数倍
    val cam_module: String = IOTConstants.NULL_KEY, //抓拍频率  60S的整数倍
    val gnss_module: String = IOTConstants.NULL_KEY, //单点定位频率  60S的整数倍
    val qj_module: String = IOTConstants.NULL_KEY, //倾角采集频率  60S的整数倍
    val flows_module: String = IOTConstants.NULL_KEY, //流速采集频率  60S的整数倍
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
