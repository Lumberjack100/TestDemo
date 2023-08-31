package com.shmedo.lib.device.base.iot_cmd.entity

import android.text.TextUtils
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/3 <br></br>
 * 描述：     语音播报参数
 */
class BroadcastEntity : Validater {
    var b_num = 0// 播报遍数
    var b_size = 0//播报内容大学
    var b_content: String? = null//播报内容 utf-8


    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("b_num=$b_num")
            append("&")
            append("b_size=$b_size")
            append("&")
            append("b_content=" + if (TextUtils.isEmpty(b_content)) "" else b_content)
        }

        return stringBuilder.toString()
    }
}