package com.shmedo.configlibrary.ble.utils;


import android.text.TextUtils;

import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.enums.CommandType;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class StringUtil {

    public static boolean isNullOrEmptyList(List<String> list) {
        return list.size() == 0;
    }

    /**
     * 从DAS返回结果中提取指令类型
     *
     * @param result $$000\r\n类似格式的DAS返回结果
     * @return 从DAS返回结果中提取指令类型
     */
    public static CommandType extractCommandType(String result) {
        if (TextUtils.isEmpty(result) || result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);

        String cmd = result.replace(CommandResult.COMMAND_RESULT_HEADER, "").substring(0, 3);
        Holder<CommandType> cmdTypeHolder = new Holder<>();
        for (CommandType commandType : CommandType.values()) {
            if (commandType.toString().equals(cmd)) {
                cmdTypeHolder.setData(commandType);
            }
        }

        CommandType cmdType = cmdTypeHolder.getData();
        if (cmdType == null) {
//            throw new IllegalArgumentException("未找到命令:" + result);
            Timber.e("未找到命令:" + result);
            return CommandType.UNKNOWN;
        }

        return cmdType;
    }


    /**
     * A-Z 65-90 a-z 97-122
     *
     * @return 返回六位随机数
     */
    public static String getRandomString() {
        StringBuilder builder = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; ++i) {
            char c = (char) (rnd.nextInt(90) % (90 - 65 + 1) + 65);
            char c2 = (char) (rnd.nextInt(122) % (122 - 97 + 1) + 97);
            char use = i % 2 == 0 ? c : c2;
            builder.append(use);
        }
        return builder.toString();
    }


    /**
     * 将字节数组转化成字符串
     *
     * @param src 字节数组
     * @return 返回字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将十六进制字符串转化成数组
     *
     * @param hexString 参数
     * @return 返回字节数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将字符串转化成十六进制字符串
     *
     * @param str 返回字符串
     * @return 返回字符串
     */
    public static String convertStringToHex(String str) {
        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]) + " ");
        }

        return hex.toString();
    }

    /**
     * 倒叙
     *
     * @param str 参数
     * @return 返回字符串
     */
    public static String reverseString(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = str.length() - 1; i >= 0; i--) {
            stringBuffer.append(str.charAt(i));
        }
        return stringBuffer.toString();
    }


    /**
     * 格式化输出2位整数(02d: 0 代表前面补充0,2 代表长度为2,d 代表参数为正数型)
     *
     * @param str
     * @return
     */
    public static String formatStringTwo(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return String.format(Locale.getDefault(), "%02d", Integer.parseInt(str));
    }

    /**
     * 格式化输出4位整数(04d: 0 代表前面补充0,4 代表长度为4,d 代表参数为正数型)
     *
     * @param str
     * @return
     */
    public static String formatStringFour(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return String.format(Locale.getDefault(), "%04d", Integer.parseInt(str));
    }

    /**
     * 格式化输出5位整数(05d: 0 代表前面补充0,5 代表长度为5,d 代表参数为正数型)
     *
     * @param str
     * @return
     */
    public static String formatStringFive(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return String.format(Locale.getDefault(), "%05d", Integer.parseInt(str));
    }


    public static int formatNumber(String accessSum) {
        if (accessSum != null) {
            return Integer.parseInt(accessSum);
        } else {
            return -1;
        }
    }

    /**
     * 将 String 转换的double保留3位小数
     * @param param
     * @return
     */
    public static String getDouble3AccuracyString(String param) {
        String result = "";
        try {
            result = String.format(Locale.getDefault(), "%.3f", Double.parseDouble(param));
        } catch (Exception ex) {
            result = "";
            ex.printStackTrace();
        }

        return result;
    }

    public static String getByteSize(int size) {
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else {
            resultSize = "< 1MB";
        }


        return resultSize;
    }
}
