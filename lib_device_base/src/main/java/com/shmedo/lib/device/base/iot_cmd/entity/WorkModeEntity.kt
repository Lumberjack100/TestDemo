package com.shmedo.lib.device.base.iot_cmd.entity

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：       生成ADME 工作模式参数拼接指令
 */
class WorkModeEntity(
    var mode //工作模式(1:正常工作模式，2:低功耗模式)
    : String = "1"
) : Validater {
    override fun validate() {}

    override fun toString(): String {
        return "mode=$mode"
    }
}