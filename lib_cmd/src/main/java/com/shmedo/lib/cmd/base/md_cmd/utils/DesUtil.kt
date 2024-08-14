package com.shmedo.lib.cmd.base.md_cmd.utils

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： DES加密和解密过程中，密钥长度都必须是8的倍数
 */
object DesUtil {
    /**
     * 加密
     *
     * @param dataSource ByteArray
     * @param password String
     * @return ByteArray
     */
    fun encrypt(dataSource: ByteArray, password: String): ByteArray? {
        return try {
            val keyByte = password.toByteArray(StandardCharsets.UTF_8)
            val desKey = DESKeySpec(keyByte)
            //创建一个密匙工厂，然后用它把DESKeySpec转换成
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val securekey = keyFactory.generateSecret(desKey)
            //Cipher对象实际完成加密操作
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
            //用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, IvParameterSpec(keyByte))
            //现在，获取数据并加密
            //正式执行加密操作
            cipher.doFinal(dataSource)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 解密
     *
     * @param src ByteArray
     * @param password String
     * @return ByteArray
     */
    fun decrypt(src: ByteArray?, password: String): ByteArray? {
        return try {
            val keyByte = password.toByteArray(StandardCharsets.UTF_8)
            // 创建一个DESKeySpec对象
            val desKey = DESKeySpec(keyByte)
            // 创建一个密匙工厂
            val keyFactory = SecretKeyFactory.getInstance("DES")
            // 将DESKeySpec对象转换成SecretKey对象
            val securekey = keyFactory.generateSecret(desKey)
            // Cipher对象实际完成解密操作
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, IvParameterSpec(keyByte))
            // 真正开始解密操作
            cipher.doFinal(src)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
