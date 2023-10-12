package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     MR 设备的模块状态
 */
data class MRModuleStatusInfo(
    var screen: String = "",//触摸屏  1 正常  0 异常
    var datanet: String = "", //4G模块  1 正常  0 异常
    var beidou: String = "",//北斗定位模块  1 正常  0 异常
    var wirednet: String = "",//有线模块  1 正常  0 异常
    var flash: String = "",//flash  1 正常  0 异常
    var emmc: String = "",//EMMC存储模块  1 正常  0 异常
)