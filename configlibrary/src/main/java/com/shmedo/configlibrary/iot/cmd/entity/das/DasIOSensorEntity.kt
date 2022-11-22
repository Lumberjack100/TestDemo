package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：      生成DAS 开关量传感器参数拼接指令
 */
class DasIOSensorEntity : Validater {
    var type //0：关闭开关量功能 1：雨量站模式 2：断线报警器模式
            : String? = null
    var value //当type取1时，value代表雨量计精度  当type取2时，value代表断线报警器状态，0：常开，1：常关
            : String? = null
    var min_time //雨量计翻斗翻转最小间隔
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