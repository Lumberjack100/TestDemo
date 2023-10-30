package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：    RS232-1-摄像头参数
 */
data class MRRS232Port1Param(
    var status: String = "",//状态  1 已接入 0未接入
    var switch: String = "",//开关  1 开 0关
    var type: String = "",//摄像头类型   1: 类型1  2: 类型2 3: 类型3
    var resolut: String = "",//分辨率  1 分辨率1  2 分辨率2 3 分辨率3
    var interval: String = "",//拍照间隔  s  数字
    var baud: String = "",//波特率  bps 数字
    var databit: String = "",//数据位   数字(5 6 7 8)
    var parity : String = "",//校验位 1  NONE  2 ODD  3 EVEN  4 MARK 5 SPACE
    var stopbit  : String = "",//停止位   1: 1  2: 1.5  3: 2  或 数字(1 1.5 2)
)
