package com.shmedo.lib.device.base.iot_cmd.entity.vms

import com.shmedo.lib.device.base.iot_cmd.enums.VmsAisleNumber
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/15/20 <br></br>
 * 描述：    Vms网关通道参数
 */
class VmsAisleParamEntity : Validater {
    var vmsAisleNumber: VmsAisleNumber? = null
    var netid //网关配置通道的网络号,取值[1,65535]，默认为1
            : String? = null
    var ppt //空中唤醒时间，取值[0,5]s，默认2，当为0时，lora模块不休眠
            : String? = null
    var addr //网关配置通道的地址,取值[0,63]，channel=0，默认为1；channel=1，默认为2；channel=2，默认为3
            : String? = null
    var chl //通信信道，取值[0,31]，channel=0，默认为23；channel=1，默认为20；channel=2，默认为26
            : String? = null
    var terminalmode //终端工作模式，取值[0,1],0低功耗模式，1正常模式
            : String? = null
    var sendgap //指令发送间隔，取值≥3s，默认为3
            : String? = null
    var offline //存活时间（终端长时间无数据，网关将终端删除），取值≥7200s，默认43200s
            : String? = null
    var sleepgap //终端休眠时间，取值[0,5]s，默认为2
            : String? = null
    var wakeupgap //终端唤醒时间，取值[0,65535]ms，默认100
            : String? = null
    var airbaud //空中速率，取值[1~6]
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("channel")
            stringBuilder.append("=")
            vmsAisleNumber?.let { stringBuilder.append(it.toInt()) }
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && f.name != "vmsAisleNumber" && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}