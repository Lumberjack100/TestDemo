package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/27/20 <br></br>
 * 描述：    生成ADME 计米轮配置参数拼接指令
 */
class AdmeMeterWheelEntity : Validater {
    var enclinenum //编码器线数
            : String? = null
    var outline //外径
            : String? = null
    var uptiona //上拉一次修正参数
            : String? = null
    var uptionb //上拉二次修正参数
            : String? = null
    var upconstant //上拉常数
            : String? = null
    var upfilter //上拉滤波器系数
            : String? = null
    var downtiona //下放一次修正参数
            : String? = null
    var downtionb //下放二次修正参数
            : String? = null
    var downconstant //下放常数
            : String? = null
    var downfilter //下放滤波器系数
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