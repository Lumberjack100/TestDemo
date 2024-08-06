package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： 电台通讯参数信息
 */
data class RadioCommunicateInfo(
    var airbaud: String = "", //空中速率  0,1,2 默认 1
    var rxchl: String = "",//接收频率 [0~19] 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 自组网网关：接收默认6，发送默认13 M20S：接收默认13，发送默认6
    var txchl: String = "",//发送频率 [0~19] 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 自组网网关：接收默认6，发送默认13 M20S：接收默认13，发送默认6
    var outpwr: String = "",//发射功率 [0~22] 默认22
    var bcchl: String = "",//广播频率 [1~19] 默认1 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道（仅M20S有效，默认为0）
)
