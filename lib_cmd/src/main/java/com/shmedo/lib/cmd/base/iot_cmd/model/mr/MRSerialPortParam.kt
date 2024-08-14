package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：    MR 串口参数
 */
data class MRSerialPortParam(
    var baud: String = "",//波特率
    var parity: String = "",//校验位
    var databit: String = "",//数据位
    var stopbit: String = "",//停止位
)
