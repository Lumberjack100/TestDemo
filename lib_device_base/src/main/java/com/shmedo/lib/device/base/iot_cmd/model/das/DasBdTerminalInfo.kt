package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/20 <br></br>
 * 描述：     北斗数传终端
 */
data class DasBdTerminalInfo(
    var sw: String = "",//0 关闭 1 开启
    var dstaddr: String = "", //目标地址
    var baud: String = "" //波特率
)