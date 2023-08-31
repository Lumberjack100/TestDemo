package com.shmedo.lib.device.base.iot_cmd.entity

import android.text.TextUtils
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：   生成设备日志输出物联网拼接指令
 */
class IotLogOutputEntity : Validater {
     var level  : String? = null//日志输出等级包含off、debug、info
     var type : String? = null //输出方式包括uart、bt、net、file


    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("level=$level")
            append("&")
            if (!TextUtils.isEmpty(type)) {
                append("type=$type")
                append("&")
            }
            if (toString().endsWith("&")) {
              delete(length - 1, length)
            }
        }

        return stringBuilder.toString()
    }
}