package com.shmedo.lib.device.base.iot_cmd.enums

import java.io.Serializable

/**
 * Vms网关通道编号
 */
enum class VmsAisleNumber(private val number: Int) : Serializable {
    NUMBER_ONE(0), NUMBER_TWO(1), NUMBER_THREE(2);

    fun toInt(): Int {
        return number
    }

    companion object {
        fun value(port: Int) = when (port) {
            0 -> NUMBER_ONE
            1 -> NUMBER_TWO
            2 -> NUMBER_THREE
            else -> NUMBER_ONE
        }
    }
}