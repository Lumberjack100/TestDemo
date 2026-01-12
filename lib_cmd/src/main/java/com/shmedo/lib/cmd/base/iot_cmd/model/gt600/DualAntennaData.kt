package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: 双天线参数查询响应模型
 *
 * 对应指令: md_cfgnmeavtgout&method=0
 * 应答示例: $cmd=md_cfgnmeavtgout&method=0&switch=1&antdist=50.44&report_freq=1
 *
 * 参数说明:
 * - switch: 功能开关 (0-关闭, 1-开启)
 * - antdist: 天线距离，单位厘米，数值大于零
 * - report_freq: 输出频率，单位秒，支持 1,5,10,15,30,60 选择
 */
@JsonClass(generateAdapter = true)
data class DualAntennaData(
    var method: String = "0",        // 功能选择: 0-获取参数
    var switch: String = "0",        // 功能开关: 0-关闭(默认), 1-开启
    var antdist: String = "",        // 天线距离(厘米)
    var report_freq: String = ""     // 上报频率(秒)
)
