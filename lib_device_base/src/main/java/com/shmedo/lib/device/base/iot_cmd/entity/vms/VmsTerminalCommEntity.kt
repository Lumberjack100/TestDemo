package com.shmedo.lib.device.base.iot_cmd.entity.vms

import com.shmedo.lib.device.base.Validater


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：     Vms 终端通信参数
 */
class VmsTerminalCommEntity : Validater {
    var sn: String? = null
    var netid //网络号
            : String? = null
    var dstaddr //通道的地址
            : String? = null
    var channel //通信信道
            : String? = null
    var airbaud //空中波特率
            : String? = null

    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("sn")
            stringBuilder.append("=")
            stringBuilder.append(sn)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (f.name != "sn" && value != null && value != "NullKey") {
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