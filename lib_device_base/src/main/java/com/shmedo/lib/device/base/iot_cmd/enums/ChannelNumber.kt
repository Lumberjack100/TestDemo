package com.shmedo.lib.device.base.iot_cmd.enums

import java.io.Serializable

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/16/20 <br></br>
 * 描述：     通道编号
 */
enum class ChannelNumber(private val number: Int) : Serializable {
    NUMBER_ONE(1), NUMBER_TWO(2), NUMBER_THREE(3), NUMBER_FOUR(4);

    fun toInt(): Int {
        return number
    }

    companion object {
        @JvmStatic
        fun value(port: Int) = when (port) {
            1 -> NUMBER_ONE
            2 -> NUMBER_TWO
            3 -> NUMBER_THREE
            4 -> NUMBER_FOUR
            else -> NUMBER_ONE
        }
    }
}