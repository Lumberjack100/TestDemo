package com.shmedo.lib.device.base.iot_cmd.entity.adme

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/6/21 <br></br>
 * 描述：     生成ADME测量孔深配置参数拼接指令
 */
class AdmeMeasuringHoleDepthEntity : Validater {
    var movementway //运动方式（0:上拉，1:下放）
            : String? = null
    var motorspeed //电机速度
            : String? = null
    var movedistance //运动距离
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