package com.shmedo.lib.device.base.md_cmd.enums

/**
 * 创建者：gonghe
 * 创建时间：2024/4/16
 * 描述： 断线报警器状态 0：常开，1：常关
 */
enum class MDBreakAlarmStatus(private val status: String) {
    QUERY("0"),

    /**
     * 常开
     */
    OPEN("1"),

    /**
     * 常关
     */
    CLOSE("2");

    override fun toString(): String {
        return status
    }

    companion object {
        fun value(status: String): MDBreakAlarmStatus {
            return when (status) {
                "0" -> QUERY
                "1" -> OPEN
                else -> CLOSE
            }
        }
    }
}