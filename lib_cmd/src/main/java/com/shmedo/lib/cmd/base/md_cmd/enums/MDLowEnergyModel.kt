package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述：  DAS低功耗模式
 */
enum class MDLowEnergyModel(private val model: String) {

    STANDBY("1"),  //待机
    ACTIVATE("2"); //激活

    override fun toString(): String {
        return model
    }

    companion object {
        @JvmStatic
        fun value(value: String) = when (value) {
            "2" -> ACTIVATE
            else -> STANDBY
        }
    }
}