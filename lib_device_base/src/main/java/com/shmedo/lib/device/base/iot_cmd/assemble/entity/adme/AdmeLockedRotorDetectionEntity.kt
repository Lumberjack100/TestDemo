package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/18
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AdmeLockedRotorDetectionEntity (
    val lowtbtss: String = "", //下放堵转缓停（0:关闭，1:开启）
    val numpput: String = "", //单位时间脉冲数
    val pdajtime : String = "",//脉冲检测判断时间
    val detintiona: String = "", //堵转检测区间起始值
    val detintionb : String = "",//堵转检测区间终值
    val lowtorblothr : String = "",//下放力矩堵转阈值
    val lowtordetime: String = "", //下放力矩检测判断时间
    val lowsusrana: String = "", //下放缓停区间起始值
    val lowsusranb: String = "", //下放缓起区间终值
    val uptbtss : String = "",//上拉堵转缓停（0:关闭，1:开启）
    val uptorblothr: String = "", //上拉力矩堵转阈值
    val uptordetime: String = "", //上拉力矩检测判断时间
    val upsusrana : String = "",//上拉缓停区间起始值
    val upsusranb: String = "", //上拉缓起区间终值
)
{
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}