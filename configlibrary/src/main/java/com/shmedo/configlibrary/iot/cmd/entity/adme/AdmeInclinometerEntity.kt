package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：    生成ADME 测斜仪配置参数拼接指令
 */
class AdmeInclinometerEntity : Validater {
    var inctype //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
            : String? = null
    var lowpower //低功耗模式(0:关闭，1:开启)
            : String? = null
    var address //采集器 / MAC 地址
            : String? = null
    var collinval //采集器采集间隔
            : String? = null
    var calcinval //采集器解算间隔
            : String? = null
    var dormancytime //休眠时间
            : String? = null
    var interupdate //测斜仪修正值
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