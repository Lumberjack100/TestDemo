package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     4G 5G 网络信息
 */
data class MRWirelessNet(
    var switch: String = "1",//是否开启4G 0:关闭 1:开启
    var apn: String = "",
    var username: String = "",
    var password: String = ""
)