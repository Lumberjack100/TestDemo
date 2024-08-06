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
data class AdmeVoltageConfigEntity (
    val volt_power_standard: String = "", //驱动器标压阈值
    val volt_power_low: String = "", //驱动器低压阈值
    val volt_power_under: String = "", //驱动器欠压阈值
    val volt_sensor_standard: String = "", //测斜仪标压阈值
    val volt_sensor_low: String = "", //测斜仪低压阈值
    val volt_sensor_under: String = "", //测斜仪欠压阈值
    val rope_length: String = "", //钢丝绳长
    val antifdis: String = IOTConstants.NULL_KEY, //防冻距离
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
