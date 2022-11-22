package com.shmedo.configlibrary.iot.cmd.entity.vms

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：     Vms 终端采集参数
 */
class VmsTerminalCollectorEntity : Validater {
    var sn: String? = null
    var reptgap //数据上报间隔，单位s
            : String? = null
    var repttype //数据上报方式，0：网关召测，1：主动上报
            : String? = null
    var filtertype //滤波类型，默认0（无滤波）1，中值滤波；2，算术平均滤波；3，中位值平均滤波；4，加权平均滤波
            : String? = null
    var filternum //样本大小，默认10
            : String? = null
    var collgap //采集间隔，默认10
            : String? = null
    var waitgap //激励前等待间隔，默认500，单位ms
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