package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：    生成ADME 基础配置参数拼接指令
 */
class AdmeBasicConfigEntity : Validater {
    var inctype //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
            : String? = null
    var address //采集器 / MAC 地址
            : String? = null
    var interdeep //测斜管孔深
            : String? = null
    var downspeed //下放速度
            : String? = null
    var downwaitetime //下放等待时间
            : String? = null
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && value != "NullKey") {
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