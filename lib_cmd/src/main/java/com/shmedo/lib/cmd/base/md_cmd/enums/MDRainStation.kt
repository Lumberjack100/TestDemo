package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * Created by adu on 2017/12/15.
 * 雨量站
 */
enum class MDRainStation(private val state: String) {
    /**
     * 雨量计开启
     */
    RAIN_OPEN("1"),

    /**
     * 关闭
     */
    CLOSE("2"),

    /**
     * 断线报警器打开
     */
    ALARM_OPEN("3");

    override fun toString(): String {
        return state
    }


    companion object {
        fun value(state: String): MDRainStation {
            return when (state) {
                "1" -> RAIN_OPEN
                "2" -> CLOSE
                "3" -> ALARM_OPEN
                else -> CLOSE
            }
        }
    }
}
