package com.shmedo.lib.device.base.iot_cmd.entity.adme

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：       生成ADME 工作模式参数拼接指令
 */
class AdmeWorkModeEntity(
    var workmode //工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)
    : String = "0"
) : Validater {
    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("workmode=").append(workmode)
        return stringBuilder.toString()
    }
}