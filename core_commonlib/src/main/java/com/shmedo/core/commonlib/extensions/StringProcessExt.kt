package com.shmedo.core.commonlib.extensions

import java.nio.charset.Charset
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/12
 * 描述： TODO
 */


inline fun <reified T, V> T.compareAndReturn(expected: T, thenValue: V, elseValue: V): V {
    return if (this == expected) thenValue else elseValue
}

fun String.stringToGBK16UByteString(): String {
    val gbkBytes = stringToGBKByteArray()
    return gbkBytes.joinToString(separator = "") { it.toUByte().toString(16).padStart(2, '0') }
        .uppercase()
}

fun String.stringToGBKByteArray(): ByteArray {
    // 将字符串按照 GBK 编码转换为字节数组
    return this.toByteArray(Charset.forName("GBK"))
}

fun String.gbkHexToString(): String {
    if (this.isEmpty()) return ""

    // 将十六进制字符串转换为字节数组
    val gbkBytes = this.hexStringToByteArray()
    // 将字节数组按照 GBK 编码解码为字符串
    return String(gbkBytes, Charset.forName("GBK"))
}

fun String.hexStringToByteArray(): ByteArray {
    // 将十六进制字符串转换为字节数组
    val len = this.length
    val data = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        data[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
    }
    return data
}

/**
 * 将十进制数字字符串转换为十六进制数字字符串
 */
fun String.decimalStringToHexString(): String {
    return this.toIntOrNull()?.toString(16)?.uppercase(Locale.ROOT) ?: ""
}

/**
 * 将十六进制数字字符串转换为十进制数字字符串
 */
fun String.hexStringToDecimalString(): String {
    return this.toInt(16).toString()
}


