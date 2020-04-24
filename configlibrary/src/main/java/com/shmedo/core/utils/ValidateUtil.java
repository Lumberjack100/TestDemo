package com.shmedo.core.utils;


import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by adu on 2017/12/11.
 * 验证
 */
public class ValidateUtil {
    /**
     * 正则表达式：验证手机号
     */
    private static final String REGEX_MOBILE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";

    /**
     * 正则表达式：验证数字
     */
    private static final String REGEX_NUMERIC = "^[0-9]*$";

    /**
     * 正则表达式：验证整数
     */
    private static final String REGEX_INTEGER = "^[+-]?[0-9]+$";

    /**
     * 正则表达式：验证双精度浮点数
     */
    private static final String REGEX_DOUBLE = "^[+-]?[0-9.]+$";

    /**
     * 正则表达式：URL
     */
    public static final String REGEX_URL = "[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";


    /***
     * 判断 String 是否是 int<br>通过正则表达式判断
     *
     * @param input
     * @return
     */
    public static boolean isInteger(String input){
        return Pattern.matches(REGEX_INTEGER, input);
    }


    /**
     * 判断 String 是否是 double<br>通过正则表达式判断
     * @param input
     * @return
     */
    public static boolean isDouble(String input){
        return Pattern.matches(REGEX_DOUBLE, input);
    }

    public static boolean isNumeric(String str) {
        return Pattern.matches(REGEX_NUMERIC, str);
    }

    /**
     * 验证长度是否6位
     *
     * @param str 需要判断的参数
     * @return 返回是否是六位数字
     * @throws DASParameterException 参数异常
     */
    public static boolean isNumberSix(String str) throws DASParameterException {
        if (str.trim().length() != 6) {
            throw new DASParameterException("参数异常");
        }

        return Pattern.matches(REGEX_NUMERIC, str);
    }

    /**
     * 验证长度是否8位
     *
     * @param str 需要判断的参数
     * @return 返回是否是六位数字
     * @throws DASParameterException 参数异常
     */
    public static boolean isNumberEight(String str) throws DASParameterException {
        if (str.trim().length() != 8) {
            throw new DASParameterException("参数异常");
        }

        return Pattern.matches(REGEX_NUMERIC, str);
    }

    /**
     * 验证手机号码
     * <p>
     * 移动号码段:139、138、137、136、135、134、150、151、152、157、158、159、182、183、187、188、147
     * 联通号码段:130、131、132、136、185、186、145
     * 电信号码段:133、153、180、189
     *
     * @param phoneNumber 需要判断的手机号码
     * @return 返回是否是手机号
     */
    public static boolean checkMobileNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        return Pattern.matches(REGEX_MOBILE, phoneNumber);
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

    /**
     * 验证本地时间
     *
     * @param time 需要判断的时间
     * @return 返回时间的格式是否正确
     */
    public static boolean cheakLocalTime(String time) {
        boolean checkSuccess = false;
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        try {
            Date date = format.parse(time);
            String str = format.format(date);
            if (str.equals(time))
                checkSuccess = true;
        } catch (ParseException e) {
            e.printStackTrace();
            return checkSuccess;
        }
        return checkSuccess;
    }

}
