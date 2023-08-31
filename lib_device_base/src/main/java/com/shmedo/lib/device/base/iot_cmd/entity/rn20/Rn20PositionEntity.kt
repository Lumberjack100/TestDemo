package com.shmedo.lib.device.base.iot_cmd.entity.rn20

import com.shmedo.lib.device.base.Validater


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/4 <br></br>
 * 描述：     TODO
 */
class Rn20PositionEntity : Validater {
     var longitude: String? = null
     var latitude: String? = null


    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("longitude=$longitude")
            append("&")
            append("latitude=$latitude")
            if (toString().endsWith("&")) {
                delete(length - 1, length)
            }
        }

        return stringBuilder.toString()
    }
}