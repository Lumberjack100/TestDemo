package com.shmedo.lib.device.base.iot_cmd.entity.adme

import android.text.TextUtils
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/10/22 <br></br>
 * 描述：     TODO
 */
class AdmeAutoMeasuringHoleDepthEntity : Validater {
    var motorspeed //电机下放速度
            : String? = null
    var safedistance //安全距离补偿
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("motorspeed=$motorspeed")
            append("&")
            if (!TextUtils.isEmpty(safedistance)) {
                append("safedistance=$safedistance")
                append("&")
            }
            if (toString().endsWith("&")) {
                delete(length - 1, length)
            }
        }

        return stringBuilder.toString()
    }
}