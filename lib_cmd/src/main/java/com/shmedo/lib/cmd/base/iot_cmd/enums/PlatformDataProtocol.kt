package com.shmedo.lib.cmd.base.iot_cmd.enums

/**
 * @author：gonghe
 * @time: 2025/6/20
 * @desc: 平台数据协议
 * "MQTT", "TCP-C", "SL651", "NTRIP", "HTTP"
 */
enum class PlatformDataProtocol(val code: String) {

    MQTT("MQTT"),

    TCP_C("TCP-C"),

    SL651("SL651"),

    NTRIP("NTRIP"),

    HTTP("HTTP"),


    ;

    override fun toString(): String {
        return code
    }

}
