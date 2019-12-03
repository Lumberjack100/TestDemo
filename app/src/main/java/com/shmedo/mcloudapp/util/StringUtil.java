package com.shmedo.mcloudapp.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.entity.ble.collector.MqttConfigInfoSub;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.util
 * 文件名:   StringUtil
 * 创建者:   yuchao
 * 创建时间:  2016/11/10 16:52
 * 描述：    字符串工具类
 */

public class StringUtil {

    /**
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^(1[3-9])\\d{9}$";//"^1\\d{10}$"

    /**
     * 正则表达式：数字验证码
     */
    public static final String REGEX_NUMERIC = "^[0-9]*$";


    /**
     * 正则表达式：URL
     */
    public static final String REGEX_URL = "[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";


    public static boolean isNullOrEmpty(String s) {
        return (s == null) || (s.length() == 0);
    }


    /**
     * 判断集合是否为null或者0个元素
     *
     * @param c
     * @return
     */
    public static boolean isNullOrEmpty(Collection c) {
        if (null == c || c.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.equalsIgnoreCase("null");
    }

    /**
     * 手机号输入是否正确
     *
     * @param phoneNumber
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        return Pattern.matches(REGEX_MOBILE, phoneNumber);
    }


    /**
     * 是否数字
     *
     * @param code
     * @return
     */
    public static boolean isNumeric(String code) {
        if (TextUtils.isEmpty(code)) {
            return false;
        }

        return Pattern.matches(REGEX_NUMERIC, code);
    }


    /**
     * 检查采集器地址是否合法；地址可以为空
     *
     * @param address
     * @return
     */
    public static boolean checkServerAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return true;
        }

        if(!address.contains(":")){
            return false;
        }

        return Pattern.matches(REGEX_URL, address);
    }

    public static String convertStringToHex(String str) {

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]) + " ");
        }

        return hex.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String formatStringTwo(String str) {
        if (StringUtil.isEmpty(str)) {
            return "";
        } else {
            return String.format("%02d", Integer.valueOf(str));
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatStringFour(String str) {
        if (StringUtil.isEmpty(str)) {
            return "";
        } else {
            return String.format("%04d", Integer.valueOf(str));
        }
    }

    public static String formatStringFive(String str) {
        if (StringUtil.isEmpty(str)) {
            return "";
        } else {
            return String.format("%05d", Integer.valueOf(str));
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatTwo(int accessSum) {
        if (accessSum == 0) {
            return "00";
        } else {
            int sum = accessSum;
            return String.format("%02d", sum);
        }
    }

    public static int formatNumber(String accessSum) {
        if (accessSum != null) {
            return Integer.valueOf(accessSum);
        } else {
            return -1;
        }
    }

    //public static List<String> getCmdResult(List<String> result){
    //
    //    StringBuffer buffer = new StringBuffer();
    //    List<String> stringList = new ArrayList<>();
    //    for (int i = 0;i<lists.get(0).getCommands().size();i++){
    //        buffer.append(lists.get(0).getCommands().get(i).toString());
    //    }
    //    stringList.add(String.valueOf(buffer));
    //
    //    return stringList;
    //}

    public static String setSize(int size) {
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

//        else if (size / KB >= 1) {
//            //如果当前Byte的值大于等于1KB
//            resultSize = df.format(size / (float) KB) + "KB   ";
//        } else {
//            resultSize = size + "B   ";
//        }
        return resultSize;
    }

    /**
     * 提取指令类型
     * @param result
     * @return
     */
    public static String extractCommandType(String result){
        if (com.shmedo.das.utils.StringUtil.isNullOrEmpty(result) || result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        String cmd = result.replace(CommandResult.COMMAND_RESULT_HEADER, "").substring(0, 3);
        return cmd;
    }

    /**
     * 判断链路是否开启
     * @param result 蓝牙指令返回参数
     * @return 返回是否开启
     */
    public static boolean isOpenLink(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        String[] cmd = result.split(" ");
        if (cmd[1].equals("0.0.0.0")){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取链路序号
     * @param result
     * @return
     */
    public static String linkNumber(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        String[] cmd = result.split(" ");
        String number = cmd[0].replace(CommandResult.COMMAND_RESULT_HEADER, "").substring(3,4);
        return number;
    }

    /**
     * 解析mqtt config
     * @param result
     * @return
     *
     *
     * $$8893,1,4,mqtt.shmedo.com 6883,300,18A092L,18A092L,a84b42b1-cb30-410f-8285-5f4de6f9d319,
     * 2,mqtt.shmedo.com 80,fXQQROerSlJ0bqTPCoMnyqgR-2dzhytztk3eYV6nuA0OBQljkqG_exXYtNfr,,,
     *
     * $$8891,0,2,0.0.0.0 9001,,,,,,,,,,
     */
    public static MqttConfigInfoSub parserMqttConfig(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        MqttConfigInfoSub mqttConfigInfoSub = new MqttConfigInfoSub();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        mqttConfigInfoSub.setDataCenterSwitch(Integer.parseInt(cmd[1]));
        mqttConfigInfoSub.setCommunicationProtocol(cmd[2]);
        mqttConfigInfoSub.setDataPlatformAddress(cmd[3]);
        mqttConfigInfoSub.setKeepAliveValue(cmd[4]);
        mqttConfigInfoSub.setDeviceSn(cmd[5]);
        mqttConfigInfoSub.setProductId(cmd[6]);
        mqttConfigInfoSub.setRegistrationCode(cmd[7]);
        mqttConfigInfoSub.setRegistrationPlatform(cmd[8]);
        mqttConfigInfoSub.setRegistrationPlatformAddress(cmd[9]);
        mqttConfigInfoSub.setAppKey(cmd[10]);
        mqttConfigInfoSub.setMqttDeviceId(cmd[11]);
        mqttConfigInfoSub.setMqttUsername(cmd[12]);
        mqttConfigInfoSub.setMqttPassword(cmd[13]);
        return mqttConfigInfoSub;
    }


}
