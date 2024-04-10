package com.shmedo.lib.device.base.md_cmd.enums

/**
 * 断线报警器状态
 */
enum class BreakAlarmStatus(private val status: Int) {
    QUERY(0),
    OPEN(1),
    CLOSE(2);

    fun toInt(): Int {
        return status
    }

    companion object {
        fun value(status: Int): BreakAlarmStatus {
            return when (status) {
                0 -> QUERY
                1 -> OPEN
                2 -> CLOSE
                else -> OPEN
            }
        }
    }
}
