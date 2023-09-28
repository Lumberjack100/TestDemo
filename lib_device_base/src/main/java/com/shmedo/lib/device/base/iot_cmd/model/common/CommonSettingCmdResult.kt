package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/9/2 <br></br>
 * 描述：     通用设置指令响应结果实体类
 */
data class CommonSettingCmdResult(
    var isSucceed: Boolean = false,
    var reason: String = ""
)