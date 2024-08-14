package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * Created by adu on 2018/1/8.
 * 渗压计功能状态
 */
enum class MDOsmometerStatus(//1、开启      2、关闭
    private val status: String
) {
    OSMOMETER_OPEN("1"),
    OSMOMETER_CLOSE("2");

    override fun toString(): String {
        return status
    }

    fun toInt(): Int {
        return status.toInt()
    }

    companion object {
        fun value(status: String): MDOsmometerStatus {
            return when (status) {
                "1" -> OSMOMETER_OPEN
                "2" -> OSMOMETER_CLOSE
                else -> OSMOMETER_CLOSE
            }
        }
    }
}
