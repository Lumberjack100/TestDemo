package com.shmedo.lib.cmd.base.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// 统一三位小数字符串格式化器，使用固定 Locale 防止不同地区小数点符号被改写
private val threeDecimalFormat = DecimalFormat("0.000", DecimalFormatSymbols(Locale.US)).apply {
    roundingMode = RoundingMode.HALF_UP
}

/**
 * 规范化原始数值字符串：
 * 1. 先把 "nan" 等非法值归零；
 * 2. 若为空则回落至默认值；
 * 3. 再进行浮点格式化，确保最多保留三位小数。
 */
fun sanitizeDecimalValue(rawValue: String?, defaultValue: String): String {
    val candidate = rawValue
        ?.replace("nan", "0", ignoreCase = true)
        ?.takeIf { it.isNotBlank() }
        ?: defaultValue
    return candidate.formatFloatIfNeeded()
}

/**
 * 将字符串视作数值，并在满足浮点输入条件时执行三位小数四舍五入。
 * 如果四舍五入后小数部分全为 0，则输出整数字符串（避免 5.000 这类冗余格式）。
 */
fun String.formatFloatIfNeeded(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return trimmed

    val numeric = trimmed.toDoubleOrNull() ?: return trimmed

    // 仅对显式包含小数点或科学计数法的字符串进行格式化，保持纯整数输入不变
    val isFloatInput = trimmed.contains('.') || trimmed.contains('e', ignoreCase = true)
    if (!isFloatInput) return trimmed

    val formatted = threeDecimalFormat.format(numeric)
    val decimalPointIndex = formatted.indexOf('.')
    if (decimalPointIndex == -1) return formatted

    val fractionPart = formatted.substring(decimalPointIndex + 1)
    if (fractionPart.all { it == '0' }) {
        val integerPart = formatted.substring(0, decimalPointIndex)
        // -0 归一化为 0，避免出现负零
        return if (integerPart == "-0") "0" else integerPart
    }
    return formatted
}
