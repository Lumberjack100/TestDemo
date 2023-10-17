package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     MR 采集控制参数
 */
data class MRCollectionParam(
    var colladdr: String = "",//采集器地址
    var colltype: String = "",//采集器类型
    var noresp: String = "",//超时次数/无应答次数
    var collgap: String = "",//采集间隔
    var collfreq: String = "",//采集频率
    var collcycle: String = "",//采集周期
)
