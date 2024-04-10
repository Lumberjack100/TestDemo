package com.shmedo.lib.device.base.md_cmd.enums

/**
 * Created by adu on 2017/12/15.
 * 雨量站
 */
enum class RainStation(private val state: Int) {
    /**
     * 开启
     */
    OPEN(1),

    /**
     * 关闭
     */
    CLOSE(2),

    /**
     * 断线报警器打开
     */
    ALARM_OPEN(3);

    fun toInt(): Int {
        return state
    }

    companion object {
        fun value(state: Int): RainStation {
            return when (state) {
                1 -> OPEN
                2 -> CLOSE
                3 -> ALARM_OPEN
                else -> OPEN
            }
        }
    }
}
