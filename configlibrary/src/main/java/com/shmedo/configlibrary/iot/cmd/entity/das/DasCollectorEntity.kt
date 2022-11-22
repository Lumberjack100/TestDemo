package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  4/12/21 <br></br>
 * 描述：     生成 DAS 采集器参数拼接指令
 */
class DasCollectorEntity : Validater {
    var type //采集器型号
            : String? = null
    var addr //采集器地址
            : String? = null
    var collgap //采集间隔
            : String? = null
    var calcgap //解算间隔
            : String? = null
    var standbygap //待机时长
            : String? = null
    var sensornum //接入传感器个数
            : String? = null
    var sensitivity //灵敏度
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