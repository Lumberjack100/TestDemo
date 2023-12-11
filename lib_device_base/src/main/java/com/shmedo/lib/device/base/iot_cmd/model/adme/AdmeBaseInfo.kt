package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/23/20 <br></br>
 * 描述：     ADME的基本信息
 */
data class AdmeBaseInfo(
    var sn: String = "",//
    var productid: String = "",//产品型号
    var equimodel: String = "",//设备模式（0：设备配置模式，1：自动检测模式）
    var online: String = "",//在线状态（0：离线，非0:在线)
)