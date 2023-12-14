package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/14
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AdmeMeterWheelEntity(
    val enclinenum: String = "", //编码器线数
    val outline: String = "", //外径
    val uptiona: String = "", //上拉一次修正参数
    val uptionb: String = "",//上拉二次修正参数
    val upconstant: String = "", //上拉常数
    val upfilter: String = "", //上拉滤波器系数
    val downtiona: String = "", //下放一次修正参数
    val downtionb: String = "",//下放二次修正参数
    val downconstant: String = "", //下放常数
    val downfilter: String = "", //下放滤波器系数
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}