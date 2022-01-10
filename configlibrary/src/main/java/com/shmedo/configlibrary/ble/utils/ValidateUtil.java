package com.shmedo.configlibrary.ble.utils;


import android.text.TextUtils;

import com.shmedo.configlibrary.ble.exception.DASParameterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by adu on 2017/12/11.
 * 验证
 */
public class ValidateUtil {
    /**
     * 正则表达式：验证手机号
     */
    private static final String REGEX_MOBILE = "^((13[0-9])|(14[0-9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9]))\\d{8}$";

    /**
     * 正则表达式：验证邮箱
     */
    private static final String REGEX_MAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 正则表达式：验证密码,以字母开头，长度在8~16之间，只能包含字母、数字和下划线
     */
    private static final String REGEX_PWD = "^[a-zA-Z]\\w{7,15}$";

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
    private static final String REGEX_DOUBLE = "^[-+]?[0-9]*\\.?[0-9]+$";

    /**
     * 正则表达式：URL
     */
    private static final String REGEX_URL = "^[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$";


    /***
     * 判断 String 是否是 int<br>通过正则表达式判断
     *
     * @param input
     * @return
     */
    public static boolean isInteger(String input) {
        return Pattern.matches(REGEX_INTEGER, input);
    }


    /**
     * 判断 String 是否是 double<br>通过正则表达式判断
     *
     * @param input
     * @return
     */
    public static boolean isDouble(String input) {
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
            return false;
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
     * 验证邮箱是否正确
     *
     * @param mail
     * @return
     */
    public static boolean checkMail(String mail) {
        if (TextUtils.isEmpty(mail)) {
            return false;
        }

        return Pattern.matches(REGEX_MAIL, mail);
    }

    /**
     * 验证密码是否正确
     *
     * @param pwd
     * @return
     */
    public static boolean checkPassword(String pwd) {
        if (TextUtils.isEmpty(pwd)) {
            return false;
        }

        return Pattern.matches(REGEX_PWD, pwd);
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

        return Pattern.matches(REGEX_URL, address);
    }

    public static boolean isValidMacAddress(String macStr) {
        if (macStr == null || macStr.equals("")) {
            return false;
        }
        String macAddressRule = "([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}";
        // 这是真正的MAC地址；正则表达式；
        if (macStr.matches(macAddressRule)) {
            Timber.i("it is a valid MAC address");
            return true;
        } else {
            Timber.e("it is not a valid MAC address!!!");
            return false;
        }
    }

    /**
     * 没有冒号的 MAC 地址校验
     *
     * @param macStr
     * @return
     */
    public static boolean isValidMacAddressNoColon(String macStr) {
        if (macStr == null || macStr.equals("")) {
            return false;
        }
        String macAddressRule = "[A-F0-9]{12}";
        // 这是真正的MAC地址；正则表达式；
        if (macStr.matches(macAddressRule)) {
            Timber.i("it is a valid MAC address");
            return true;
        } else {
            Timber.e("it is not a valid MAC address!!!");
            return false;
        }
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
