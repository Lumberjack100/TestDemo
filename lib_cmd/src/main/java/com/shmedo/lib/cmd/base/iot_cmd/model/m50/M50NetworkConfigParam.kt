package com.shmedo.lib.cmd.base.iot_cmd.model.m50

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2024/8/25 <br/>
 * 描述：     M50网络配置参数
 */
data class M50NetworkConfigParam(
    var switch: String = "1",     // 是否开启网络 0:关闭 1:开启
    var networkType: String = "1", // 0:eSIM 1:外置SIM 2:自动
    var apn: String = "",
    var username: String = "",
    var password: String = ""
) 