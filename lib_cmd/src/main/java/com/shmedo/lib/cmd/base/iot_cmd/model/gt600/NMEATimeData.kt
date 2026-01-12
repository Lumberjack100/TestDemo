package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: NMEA 参数查询响应模型
 *
 * 对应指令: md_getnmeatime
 * 应答示例: $cmd=md_getnmeatime&gga=1&rmc=1&vtg=1&gsv=1&gsa=1
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
data class NMEATimeData(
    var gga: String = "1",  // GPGGA 位置信息
    var rmc: String = "1",  // GPRMC 最简导航传输信息
    var vtg: String = "1",  // GPVGT 地面速度信息
    var gsv: String = "1",  // GPGSV 可视卫星状态
    var gsa: String = "1"   // GPGSA 参与定位卫星以及 DOP 值等信息
)
