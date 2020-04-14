package com.shmedo.core.utils;


import android.text.TextUtils;

import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.enums.CommandType;

import java.util.List;
import java.util.Random;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class StringUtil {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }
    public static boolean isNullOrEmptyList(List<String> list){
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
        for (CommandType cmds:CommandType.values()){
            if (cmds.toString().equals(cmd)) {
                cmdTypeHolder.setData(cmds);
            }
        }
        CommandType cmdType = cmdTypeHolder.getData();
        if (cmdType == null)
            throw new IllegalArgumentException("未找到命令:" + result);
        return cmdType;
    }


    /**
     * A-Z 65-90 a-z 97-122
     * @return 返回六位随机数
     */
    public static String getRandomString()
    {
        StringBuilder builder=new StringBuilder();
        Random rnd=new Random();
        for(int i=0;i<6;++i)
        {
            char c=(char)(rnd.nextInt(90)%(90-65+1)+65);
            char c2=(char)(rnd.nextInt(122)%(122-97+1)+97);
            char use=i%2==0?c:c2;
            builder.append(use);
        }
        return builder.toString();
    }

    /**
     *  将字节数组转化成字符串
     * @param src 字节数组
     * @return 返回字符串
     */
    public static String bytesToHexString(byte[] src){
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
     *  将十六进制字符串转化成数组
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
     *  倒叙
     * @param str 参数
     * @return 返回字符串
     */
    public static String reverseString(String str) {

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = str.length()-1;i >= 0;i--){
            stringBuffer.append(str.charAt(i));
        }
        return stringBuffer.toString();
    }
}
