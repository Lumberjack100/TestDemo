package com.shmedo.lib.device.base.md_cmd.enums

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述：  DAS低功耗模式
 */
enum class LowEnergyModel(private val model: Int) {

    STANDBY(1),  //待机
    ACTIVATE(2); //激活

    fun toInt(): Int {
        return model
    }

    companion object {
        @JvmStatic
        fun value(port: Int) = when (port) {
            1 -> STANDBY
            else -> ACTIVATE
        }
    }
}