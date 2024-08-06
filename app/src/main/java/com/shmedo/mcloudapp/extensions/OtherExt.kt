package com.shmedo.mcloudapp.extensions

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import java.nio.charset.Charset

/**
 * 创建者：gonghe
 * 创建时间：2024/5/12
 * 描述： TODO
 */

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}

inline fun <reified T> T.notNullKey(action: (T) -> Unit) {
    if (this != IOTConstants.NULL_KEY) {
        action.invoke(this)
    }
}

fun String.stringToGBK16UByteString(): String {
    val gbkBytes = stringToGBKByteArray()
    return gbkBytes.joinToString(separator = "") { it.toUByte().toString(16).padStart(2, '0') }.uppercase()
}

fun String.stringToGBKByteArray(): ByteArray {
    // 将字符串按照 GBK 编码转换为字节数组
    return this.toByteArray(Charset.forName("GBK"))
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

fun String.gbkHexToString(): String {
    // 将十六进制字符串转换为字节数组
    val gbkBytes = this.hexStringToByteArray()
    // 将字节数组按照 GBK 编码解码为字符串
    return String(gbkBytes, Charset.forName("GBK"))
}
