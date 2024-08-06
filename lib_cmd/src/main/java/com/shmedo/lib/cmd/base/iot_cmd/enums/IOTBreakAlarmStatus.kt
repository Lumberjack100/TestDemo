package com.shmedo.lib.cmd.base.iot_cmd.enums

/**
 * 创建者：gonghe
 * 创建时间：2024/4/16
 * 描述： 断线报警器状态 0：常开，1：常关
 */
enum class IOTBreakAlarmStatus (private val status: String){
    /**
     * 常开
     */
    OPEN("0"),

    /**
     * 常关
     */
    CLOSE("1");

    override fun toString(): String {
        return status
    }

    fun toInt(): Int {
        return status.toInt()
    }

    companion object {
        fun value(status: String): IOTBreakAlarmStatus {
            return when (status) {
                "0" -> OPEN
                "1" -> CLOSE
                else -> OPEN
            }
        }
    }
}