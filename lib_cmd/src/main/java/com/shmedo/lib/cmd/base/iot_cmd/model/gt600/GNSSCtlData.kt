package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: GNSS 控制参数（RTCM参数）查询响应模型
 *
 * 对应指令: md_getgnssctl
 * 应答示例: $cmd=md_getgnssctl&encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0
 *
 * 参数说明:
 * - encrypttype: 是否启用加密 (0-不加密, 1-软加密, 2-硬加密)
 * - rtcmobstime: RTCM 观测数据输出频率，单位秒，默认15，取值 1,5,10,15,30
 * - rtcmephtime: RTCM 星历数据输出频率，单位秒，默认60，取值 1,5,10,15,30,60
 * - onlybd: 单北斗功能 (0-不开启, 1-开启)
 */
@JsonClass(generateAdapter = true)
data class GNSSCtlData(
    var encrypttype: String = "0",  // 加密类型: 0-不加密, 1-软加密, 2-硬加密
    var rtcmobstime: String = "15", // RTCM 观测数据输出频率(秒)
    var rtcmephtime: String = "60", // RTCM 星历数据输出频率(秒)
    var onlybd: String = "0"        // 单北斗功能: 0-不开启, 1-开启
)
