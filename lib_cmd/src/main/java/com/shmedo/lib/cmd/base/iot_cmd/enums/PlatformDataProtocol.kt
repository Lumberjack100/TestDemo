package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * @author：gonghe
 * @time: 2025/6/20
 * @desc: 平台数据协议
 * "MQTT", "TCP-C", "SL651", "NTRIP", "HTTP"
 */
enum class PlatformDataProtocol(private val cmdValue: String) {

    MQTT("MQTT"),

    TCP_C("TCP-C"),

    SL651("SL651"),

    NTRIP("NTRIP"),

    NTRIP_C("NTRIP-C"),

    NTRIP_S("NTRIP-S"),

    HTTP("HTTP"),

    SZY206("SZY206"),

    MQTTS("MQTTS"),

    ;

    fun getCmdValue(): String {
        return cmdValue
    }

    override fun toString(): String {
        return cmdValue
    }

    companion object {
        @JvmStatic
        fun valueByCmdValue(cmdValue: String): PlatformDataProtocol {
            if (TextUtils.isEmpty(cmdValue)) return MQTT
            for (protocol in entries) {
                if (protocol.cmdValue == cmdValue) return protocol
            }
            return MQTT
        }

        @JvmStatic
        val protocolNames: List<String>
            get() = PlatformDataProtocol.entries.map { it.cmdValue }

    }
}
