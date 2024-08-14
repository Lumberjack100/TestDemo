package com.shmedo.lib.cmd.base.iot_cmd.enums

/**
 * Created by adu on 2017/12/15.
 * 雨量站
 */
enum class IOTRainStation(private val status: String) {
    /**
     * 关闭开关量功能
     */
    CLOSE("0"),

    /**
     * 雨量计开启
     */
    RAIN_OPEN("1"),

    /**
     * 断线报警器打开
     */
    ALARM_OPEN("2")


    ;

    override fun toString(): String {
        return status
    }

    fun toInt(): Int {
        return status.toInt()
    }

    companion object {
        fun value(state: String): IOTRainStation {
            return when (state) {
                "0" -> CLOSE
                "1" -> RAIN_OPEN
                "2" -> ALARM_OPEN
                else -> CLOSE
            }
        }
    }
}
