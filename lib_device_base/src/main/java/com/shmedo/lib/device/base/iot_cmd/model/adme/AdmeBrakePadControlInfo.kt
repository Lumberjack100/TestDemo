package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2023/2/1 <br></br>
 * 描述：     ADME 刹车片控制
 */
data class AdmeBrakePadControlInfo(
    var mode: String = "",//工作模式(0:手动，1:自动)
)