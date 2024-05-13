package com.shmedo.lib.core.ext

import java.text.DecimalFormat

/**
 * 创建者：gonghe
 * 创建时间：2024/5/12
 * 描述： TODO
 */

 fun formatDoubleValue(value: Double?, format: DecimalFormat, defaultValue: String): String {
    return value?.let { format.format(it) } ?: defaultValue
}