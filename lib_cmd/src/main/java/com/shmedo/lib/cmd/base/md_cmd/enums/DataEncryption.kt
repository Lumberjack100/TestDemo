package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * Created by adu on 2018/3/22.
 * 数据加密
 */
enum class DataEncryption(private val model: Int) {
    CLEAR(1),
    CIPHER(2);

    fun toInt(): Int {
        return model
    }

    companion object {
        fun value(model: Int): DataEncryption {
            return when (model) {
                1 -> CLEAR
                2 -> CIPHER
                else -> CLEAR
            }
        }
    }
}
