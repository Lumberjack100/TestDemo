package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/11/3 <br/>
 * 描述：     MR 485-1 采集控制参数
 */
data class MRRS485Port1CollectionParam(
    var noresp: String = "",//超时次数/无应答次数
    var collround: String = "",//采集次数
    var collfreq: String = "",//采集频率
    var collcycle: String = "",//采集周期 min
    var powerontimes: String = "",//延时时间 s
    var ngateval: String = "",//阈值次数
)
