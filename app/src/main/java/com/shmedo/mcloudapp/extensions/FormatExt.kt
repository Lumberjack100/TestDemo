package com.shmedo.mcloudapp.extensions

import java.text.DecimalFormat

/**
 * 创建者：gonghe
 * 创建时间：2024/6/6
 * 描述： TODO
 */

// 创建DecimalFormat的方法，确保线程安全
private fun getDecimalFormat(digit: Int): DecimalFormat {
    return DecimalFormat().apply {
        if (digit == 0) {
            applyPattern("0")
        } else {
            applyPattern("#.${"#".repeat(digit)}")
        }
    }
}

fun String?.formatDoubleValue(defaultValue: String = "", digit: Int = 2): String {
    // 使用getDecimalFormat方法创建DecimalFormat实例
    val decimalFormat = getDecimalFormat(digit)

    return this?.toDoubleOrNull()?.let { decimalFormat.format(it) } ?: defaultValue
}

fun Double?.formatDoubleValue(defaultValue: String = "", digit: Int = 2): String {
    // 使用getDecimalFormat方法创建DecimalFormat实例
    val decimalFormat = getDecimalFormat(digit)

    return this?.let { decimalFormat.format(it) } ?: defaultValue
}