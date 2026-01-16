package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: NMEA 参数设置实体类
 *
 * 对应指令: md_setnmeatime
 * 发送示例: $cmd=md_setnmeatime&gga=1&rmc=1&vtg=1&gsv=1&gsa=1
 *
 * 参数说明: 单位秒，取值范围如下
 * - 20Hz: 0.05
 * - 10Hz: 0.1
 * - 5Hz: 0.2
 * - 1Hz: 1
 * - 5s: 5
 * - 10s: 10
 * - 15s: 15
 * - 30s: 30
 * - 60s: 60
 * - 关闭: 0
 */
@JsonClass(generateAdapter = true)
data class NMEATimeConfigEntity(
    val gga: String = IOTConstants.NULL_KEY, // GPGGA 位置信息
    val rmc: String = IOTConstants.NULL_KEY, // GPRMC 最简导航传输信息
    val vtg: String = IOTConstants.NULL_KEY, // GPVGT 地面速度信息
    val gsv: String = IOTConstants.NULL_KEY, // GPGSV 可视卫星状态
    val gsa: String = IOTConstants.NULL_KEY  // GPGSA 参与定位卫星以及 DOP 值等信息
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: gga=1&rmc=1&vtg=1&gsv=1&gsa=1
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
