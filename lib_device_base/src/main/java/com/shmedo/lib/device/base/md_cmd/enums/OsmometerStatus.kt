package com.shmedo.lib.device.base.md_cmd.enums

/**
 * Created by adu on 2018/1/8.
 * 渗压计功能状态
 */
enum class OsmometerStatus(//1、开启      2、关闭
    private val status: Int
) {
    OSMOMETER_OPEN(1),
    OSMOMETER_CLOSE(2);

    fun toInt(): Int {
        return status
    }

    companion object {
        fun value(status: Int): OsmometerStatus {
            return when (status) {
                1 -> OSMOMETER_OPEN
                2 -> OSMOMETER_CLOSE
                else -> OSMOMETER_CLOSE
            }
        }
    }
}
