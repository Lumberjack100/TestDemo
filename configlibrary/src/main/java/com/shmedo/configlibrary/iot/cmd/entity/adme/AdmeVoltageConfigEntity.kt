package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/13 <br></br>
 * 描述：      生成ADME 电压配置参数拼接指令
 */
class AdmeVoltageConfigEntity : Validater {
    var volt_power_standard //驱动器标压阈值
            : String? = null
    var volt_power_low //驱动器低压阈值
            : String? = null
    var volt_power_under //驱动器欠压阈值
            : String? = null
    var volt_sensor_standard //测斜仪标压阈值
            : String? = null
    var volt_sensor_low //测斜仪低压阈值
            : String? = null
    var volt_sensor_under //测斜仪欠压阈值
            : String? = null
    var rope_length // 钢丝绳长
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