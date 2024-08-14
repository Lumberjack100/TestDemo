package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     以太网信息
 */
data class MRWiredNet(
    var switch: String = "1",//是否开启以太网 0:关闭 1:开启
    var dhcp: String = "1",//是否开启DHCP 0:关闭 1:开启
    var ipaddr: String = "",//IP地址
    var mask: String = "",//子网掩码
    var gateway: String = "",//网关
    var dns: String = "",//首选DNS
    var dnss: String = ""//备用DNS
)