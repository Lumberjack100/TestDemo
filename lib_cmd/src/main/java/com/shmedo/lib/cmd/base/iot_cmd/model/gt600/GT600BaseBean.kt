package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/08
 * 描述：GT600 设备基本信息 Bean
 *
 * 对应 JSON 中的 base 字段
 */
@JsonClass(generateAdapter = true)
data class GT600BaseBean(
    val sn: String = "",           // 设备编号
    val iccid: String = "",        // SIM 卡号
    val imei: String = "",         // 移动设备识别码
    val version: String = "",      // 软件版本号
    val oem: String = "",          // 板卡类型
    val volt:  String = ""        // 外接输入电压
)
