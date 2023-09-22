package com.shmedo.lib.device.base.iot_cmd.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/21 <br/>
 * 描述：     工作模式
 */
data class WorkModeBean(var mode //工作模式(1:正常工作模式，2:低功耗模式)
    : String = "1"
)