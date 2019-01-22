
package com.shmedo.mcloudapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.util
 * 文件名:   MD5Util
 * 创建者:   dpc
 * 创建时间:  2016/11/10 10:21
 * 描述：    MD5加密工具类
 */

public class MD5Util {
    /**
     * 计算字符串的MD值
     */
    public final static String MD5(String s) {
        try {
            return md5(s.getBytes("utf-8"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /* public static String fileMD5(String filePath) {
         try {
             File f = new File(filePath);
             FileInputStream fis = new FileInputStream(f);
             byte[] bs = new byte[(int) f.length()];
             fis.read(bs);
             fis.close();
             return md5(bs);
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }

     }*/
    public static String getFileMD5(String path) {
        File file=new File(path);
        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());

        return bigInt.toString(16);
    }


    private static String md5(byte[] content) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };
        try {
            byte[] btInput = content;
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 流式MD5计算
     */
    public static String fileMD5(String inputFile) {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;
            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            return byteArrayToHex(resultByteArray).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    private static String byteArrayToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        // return hs.toUpperCase();
        return hs;

        // 首先初始化一个字符数组，用来存放每个16进制字符

      /*char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'A','B','C','D','E','F' };



      // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））

      char[] resultCharArray =new char[byteArray.length * 2];

      // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去

      int index = 0;

      for (byte b : byteArray) {

         resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];

         resultCharArray[index++] = hexDigits[b& 0xf];

      }

      // 字符数组组合成字符串返回

      return new String(resultCharArray);*/

    }



}
