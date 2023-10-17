package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：    MR 串口参数
 */
data class MRSensorStatus(
    var model: String = "",//物模型
    var sta: String = "",//接入状态
    var chl: String = "",//通道号
)
