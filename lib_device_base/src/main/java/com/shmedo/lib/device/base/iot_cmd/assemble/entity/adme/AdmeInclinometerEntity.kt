package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
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
data class AdmeInclinometerEntity(
    val inctype: String = IOTConstants.NULL_KEY, //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    val incversion: String = "", //测斜仪版本（0：2.1 版本，1：3.0 版本）
    val mode: String = "", //测量工作模式(5:蓝牙关测量关，7:蓝牙开测量关，8:蓝牙关测量开，9:蓝牙开测量开)
    val swtor_angle: String = IOTConstants.NULL_KEY, //扭转角使能
    val compenway: String = "", //补偿方式（0：X+Y轴无扭转角补偿，1：X轴扭转角补偿）
    val torangle: String = "", //扭转角γ
    val address: String = "", //采集器 / MAC 地址
    val collinval: String = "", //采集器采集间隔
    val calcinval: String = "", //采集器解算间隔
    val dormancytime: String = "", //休眠时间
    val interupdate: String = "",//测斜仪修正值
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
