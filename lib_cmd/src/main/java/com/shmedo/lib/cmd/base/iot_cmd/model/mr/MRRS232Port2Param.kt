package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：    RS232-2-北斗数据终端参数
 */
data class MRRS232Port2Param(
    var status: String = "",//状态  1 已接入 0未接入
    var switch: String = "",//开关  1 开 0关
    var daddr: String = "",//目的地址   数字
    var baud: String = "",//波特率  bps 数字
    var databit: String = "",//数据位   数字(5 6 7 8)
    var parity: String = "",//校验位 0  NONE  1 ODD  2 EVEN  3 MARK 4 SPACE
    var stopbit: String = "",//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
)
