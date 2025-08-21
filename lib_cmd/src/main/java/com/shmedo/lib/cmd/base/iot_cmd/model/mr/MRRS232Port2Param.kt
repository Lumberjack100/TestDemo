package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：    RS232-2-北斗数据终端参数
 */
data class MRRS232Port2Param(
    var sw: String = "",//开关 0   0关  1 开
    var destaddr: String = "",//目标卡号  北斗发送的目标地址，接收端的北斗卡号
    var baud: String = "",//波特率  115200  支持2400-115200
    var databit: String = "",//数据位   数字(5 6 7 8)
    var paritybit: String = "",//校验位 0  NONE  1 ODD  2 EVEN  3 MARK 4 SPACE
    var stopbit: String = "",//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    var linkid: String = "",//数据中心号 1  链路号，0~4分别对应链路1~5
    var confirm: String = "",//是否需要  1  1：不需要 2：需要
    var codetype: String = "",//编码类型 3   1：汉字， 2：ASCII， 3：混编， 4：压缩汉字， 5：压缩ASCII， 目前只支持3，即混编
)
