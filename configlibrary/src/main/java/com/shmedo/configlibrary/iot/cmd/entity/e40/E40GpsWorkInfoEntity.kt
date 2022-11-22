package com.shmedo.configlibrary.iot.cmd.entity.e40

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/6/18 <br></br>
 * 描述：       生成GPS 工作参数拼接指令
 */
class E40GpsWorkInfoEntity : Validater {
    var cutoffangle //卫星仰角截止角 范围0-90度
            : String? = null
    var range //观测范围  0或1
            : String? = null
    var savefreq //数据频率
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