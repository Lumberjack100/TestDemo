package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * Created by adu on 2017/12/11.
 * 数据通讯模式
 */
enum class MDCommunicateMode(private val mode: Int) {
    /**
     * GPRS模式
     */
    GPRS(1),

    /**
     * 短信息模式
     */
    SMS(2),

    /**
     * 北斗短报文模式
     */
    BD(3),

    BD4G(4);

    fun toInt(): Int {
        return mode
    }

    companion object {
        fun value(mode: Int): MDCommunicateMode {
            return when (mode) {
                1 -> GPRS
                2 -> SMS
                3 -> BD
                4 -> BD4G
                else -> GPRS
            }
        }
    }
}
