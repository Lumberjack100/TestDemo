package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：     生成DAS 数字水位计参数拼接指令
 */
class DasDigitalPiezometerEntity : Validater {
    var sw //0：关闭数字水位计采集功能 1：打开数字水位计采集功能
            : String? = null
    var addr //地址
            : String? = null
    var threshold //触发阈值
            : String? = null
    var corrval //修正值
            : String? = null
    var ropelen //绳长（渗压计到管口的距离）
            : String? = null
    var tubealti //安装高程
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