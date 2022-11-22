package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：      生成声光报警器参数拼接指令
 */
class AudibleAlarmEntity : Validater {
    var channell //通信信道
            : String? = null
    var panid //网络编号
            : String? = null
    var groupid //目标地址
            : String? = null
    var alarmtype //报警类型
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("alarmtype")
            stringBuilder.append("=")
            stringBuilder.append(alarmtype)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && f.name != "alarmtype" && value != "NullKey") {
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