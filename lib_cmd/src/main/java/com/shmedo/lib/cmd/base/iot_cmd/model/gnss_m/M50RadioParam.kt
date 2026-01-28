package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

/**
 * 创建者：gonghe
 * 创建时间：2025/10/7
 * 描述：M50 电台参数
 */
data class M50RadioParam(
    var sw: String = "",//开关  0 关闭 1 开启
    var freq_group: String = "",//通讯频率 [0~19] 频率以 470Mhz 为起始，间隔 2Mhz，进行信道划分，共划分20个信道
    var airbaud: String = "",//空中速率  2.4Kbps,19.2Kbps,76.8Kbps
    var txpower: String = "",//发射功率  [0 - 20]
    var local_addr: String = "",//本机地址  [1 - 65535]
    var target_addr: String = ""//目标地址  [0 - 65535]
)
