package com.shmedo.configlibrary.iot.cmd.entity.das

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：      生成声光报警器、电子点阵屏参数拼接指令
 */
class AudibleAlarmEntity : Validater {
    var alarmstatus //声光报警器开启状态 0 关闭 1 开启
            : String? = null
    var screenstatus //电子点阵屏开启状态 0 关闭 1 开启
            : String? = null
    var alarmaddr //声光报警器地址
            : String? = null
    var alarmtype //类型  0  降雨量  1 水位
            : String? = null
    var level1 //1级预警值
            : String? = null
    var level2 //2级预警值
            : String? = null
    var level3 //3级预警值
            : String? = null
    var playtime //播放时长
            : String? = null
    var playgap //播放间隙
            : String? = null
    var volume //音量大小
            : String? = null

    var screenaddr //电子屏地址
            : String? = null
    var showtime //电子屏显示时长
            : String? = null
    var showgap //电子屏熄屏时长
            : String? = null

    var mcuaddr //MCU 地址
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("alarmstatus")
            stringBuilder.append("=")
            stringBuilder.append(alarmstatus)
            stringBuilder.append("&")

            stringBuilder.append("screenstatus")
            stringBuilder.append("=")
            stringBuilder.append(screenstatus)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && f.name != "alarmstatus"&& f.name != "screenstatus" && value != "NullKey") {
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