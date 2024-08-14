package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/19 <br/>
 * 描述：     TODO
 */
data class MRRS485Port3SensorStatus(
    var solarid: String = "",//太阳能控制器地址
    var solarstatus: String = "",//太阳能控制器状态  1 接入 0 未接入
    var ysid: String = "",//声光报警器地址
    var ysstatus: String = "",//声光报警器状态   1 接入 0 未接入
    var ledid: String = "",//LED屏地址
    var ledstatus: String = "",//LED屏状态  1 接入 0 未接入
)
