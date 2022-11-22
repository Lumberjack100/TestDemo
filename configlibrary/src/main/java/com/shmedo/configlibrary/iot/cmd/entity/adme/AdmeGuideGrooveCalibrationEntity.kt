package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：      生成ADME导槽校准配置参数拼接指令
 */
class AdmeGuideGrooveCalibrationEntity : Validater {
    var movementway //运动方式（0:正转，1:反转）
            : String? = null
    var motorspeed //电机速度
            : String? = null
    var movepulse //运动脉冲数
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}