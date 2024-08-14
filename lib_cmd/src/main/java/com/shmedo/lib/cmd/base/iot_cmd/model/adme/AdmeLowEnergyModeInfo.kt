package com.shmedo.lib.cmd.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：      ADME 低功耗模式
 */
data class AdmeLowEnergyModeInfo (
    var mode: String = "",//工作模式(0:正常模式，1:低功耗模式)
)