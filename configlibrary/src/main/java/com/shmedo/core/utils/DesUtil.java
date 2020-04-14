package com.shmedo.core.utils;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by adu on 2018/1/10.
 * 注意：DES加密和解密过程中，密钥长度都必须是8的倍数
 */
public class DesUtil {
    /**
     * 加密
     * @param datasource byte[]
     * @param password   String
     * @return byte[]
     */
    public static byte[] encrypt(byte[] datasource, String password) {
        try {
            byte[]keyByte=password.getBytes(StandardCharsets.UTF_8);
            DESKeySpec desKey = new DESKeySpec(keyByte);
            //创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            //用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, new IvParameterSpec(keyByte));
            //现在，获取数据并加密
            //正式执行加密操作
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 解密
     *
     * @param src      byte[]
     * @param password String
     * @return byte[]
     */
    public static byte[] decrypt(byte[] src, String password) {
        try {
            byte[]keyByte=password.getBytes(StandardCharsets.UTF_8);
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(keyByte);
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, new IvParameterSpec(keyByte));
            // 真正开始解密操作
            return cipher.doFinal(src);
        }catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
