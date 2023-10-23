package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     开关1-8状态
 */
data class MRDIPortParam(
    var dstatus1: String = "",//1 开 0关
    var dstatus2: String = "",//1 开 0关
    var dstatus3: String = "",//1 开 0关
    var dstatus4: String = "",//1 开 0关
    var dstatus5: String = "",//1 开 0关
    var dstatus6: String = "",//1 开 0关
    var dstatus7: String = "",//1 开 0关
    var dstatus8: String = "",//1 开 0关
)
