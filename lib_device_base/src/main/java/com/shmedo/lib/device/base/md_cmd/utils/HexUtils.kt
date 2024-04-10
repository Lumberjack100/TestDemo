package com.shmedo.lib.device.base.md_cmd.utils

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
object HexUtils {
    /**
     * 将十六进制字符串转化成字节数组
     *
     * @param hexString 参数
     * @return 返回字节数组
     */
    fun hexStringToBytes(hexString: String): ByteArray? {
        if (hexString.isEmpty()) {
            return null
        }
        val formattedString = hexString.replace(" ", "").trim().uppercase()
        val length = formattedString.length / 2
        val hexChars = formattedString.toCharArray()
        val result = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            result[i] = (charToByte(hexChars[pos]) shl 4 or charToByte(hexChars[pos + 1])).toByte()
        }
        return result
    }

    private fun charToByte(c: Char): Int {
        return "0123456789ABCDEF".indexOf(c)
    }

    /**
     * 将字节数组转化为十六进制字符串
     *
     * @param src 字节数组
     * @return 返回十六进制字符串
     */
    fun bytesToHexString(src: ByteArray?): String? {
        if (src == null || src.isEmpty()) {
            return null
        }
        val stringBuilder = StringBuilder()
        for (b in src) {
            val v = b.toInt() and 0xFF
            val hv = v.toString(16)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    /**
     * 将16进制字符串转换为二进制字符串
     *
     * @param hexStr 16进制字符串
     * @return 二进制字符串
     */
    fun parseHexStr2Byte(hexStr: String): String? {
        if (hexStr.isEmpty()) return null
        val sint = hexStr.toInt(16)
        var bin = sint.toString(2)
        while (bin.length < 4) {
            bin = "0$bin"
        }
        return bin
    }

}