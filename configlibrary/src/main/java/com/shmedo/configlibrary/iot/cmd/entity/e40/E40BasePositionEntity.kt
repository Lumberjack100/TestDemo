package com.shmedo.configlibrary.iot.cmd.entity.e40

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/7/2 <br></br>
 * 描述：      生成 E40基站位置参数拼接指令
 */
class E40BasePositionEntity : Validater {
    var mode //1表示自动模式，2表示第一次自动获取以后采用第一次值，3表示手动模式，当为自动模式时，可不设置其他参数。
            : String? = null
    var lon //经度
            : String? = null
    var lat //纬度
            : String? = null
    var alt //高程
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