package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     MR 设备的基本信息
 */
data class MRRunningData(
    var ttime: String = "",//运行时间
    var otime: String = "", //单测运行时间
    var rebootn: String = "",//重启次数
    var ustorage: String = "",//已用存储   保留一位有效位数   单位G
    var tstorage: String = "",//总存储 保留一位有效位数   单位G
)