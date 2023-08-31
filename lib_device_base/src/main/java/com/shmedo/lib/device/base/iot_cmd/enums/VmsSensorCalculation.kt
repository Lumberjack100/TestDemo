package com.shmedo.lib.device.base.iot_cmd.enums

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/27/20 <br></br>
 * 描述：     Vms 终端接入的传感器计算方式
 */
enum class VmsSensorCalculation(private val type: String) {
    /**
     * 直线式(振弦式传感器)
     */
    LINEAR("58"),

    /**
     * 多项式(振弦式传感器)
     */
    POLYNOMIAL("55"),

    /**
     * 数字式
     */
    MEMS("80"),

    /**
     * 模数
     */
    MODULUS("81"),

    /**
     * 倍率
     */
    MAGNIFICATION("82");

    override fun toString(): String {
        return type
    }

    companion object {
        @JvmStatic
        fun value(type: String?): VmsSensorCalculation? =when (type) {
            "58" -> LINEAR
            "55" -> POLYNOMIAL
            "80" -> MEMS
            "81" -> MODULUS
            "82" -> MAGNIFICATION
            else -> null
        }
    }
}