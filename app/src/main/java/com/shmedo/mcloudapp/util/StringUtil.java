package com.shmedo.mcloudapp.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.util
 * 文件名:   StringUtil
 * 创建者:   yuchao
 * 创建时间:  2016/11/10 16:52
 * 描述：    字符串工具类
 */

public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();
    public static boolean isNullOrEmpty(String s){
        return (s==null)||(s.length()==0);
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
     * @param phoneNumber
     * @return
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        if (!phoneNumber.matches("^[0-9]*$")) {
            Log.i(TAG, "isPhoneNumber: match error--"+phoneNumber);
            return false;
        }
        if (phoneNumber.length() != 11) {
            Log.i(TAG, "isPhoneNumber: length error--"+phoneNumber);
            return false;
        }
        if (phoneNumber.indexOf(0) == '1') {
            Log.i(TAG, "isPhoneNumber: start error--"+phoneNumber);
            return false;
        }
        return true;
    }

    /**
     * 验证码输入是否正确
     * @param code
     * @return
     */
    public static boolean isCodeCorrect(String code) {
        if (TextUtils.isEmpty(code)) {
            return false;
        }
        if (!code.matches("^[0-9]*$")) {
            return false;
        }
        return true;
    }

}
