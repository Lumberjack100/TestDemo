package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     脉冲端口参数
 */
data class MRPulsePortParam(
    var switch: String = "",//开关  1 开 0关
    var workmode: String = "",//功能模式 计数 消警
    var dryaccuracy: String = "",//脉冲分辨率 默认1，整型，大于0，最大9999
    var dryelim: String = "",//消抖系数 默认5，整型，大于0，最大60.单位s
) 