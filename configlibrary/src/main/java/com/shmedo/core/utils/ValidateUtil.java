package com.shmedo.core.utils;


import com.shmedo.core.exception.DASParameterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adu on 2017/12/11.
 * 验证
 */
public class ValidateUtil {

    /**
     * 验证长度是否6位
     * @param str 需要判断的参数
     * @return  返回是否是六位数字
     * @throws DASParameterException 参数异常
     */
    public static boolean isNumberSix(String str) throws DASParameterException {
        boolean flag = false;
        if (str.trim().length() != 6){
            throw new DASParameterException("参数异常");
        }
        for(int i=0,n=str.length();i<n;i++){
            char c = str.charAt(i);
            if(c=='0'|c=='1'|c=='2'|c=='3'|c=='4'|c=='5'|c=='6'|c=='7'|c=='8'|c=='9'){
                flag =true;
            }
        }
        return flag;
    }
    /**
     * 验证长度是否6位
     * @param str 需要判断的参数
     * @return  返回是否是六位数字
     * @throws DASParameterException 参数异常
     */
    public static boolean isNumberEight(String str) throws DASParameterException {
        boolean flag = false;
        if (str.trim().length() != 8){
            throw new DASParameterException("参数异常");
        }
        for(int i=0,n=str.length();i<n;i++){
            char c = str.charAt(i);
            if(c=='0'|c=='1'|c=='2'|c=='3'|c=='4'|c=='5'|c=='6'|c=='7'|c=='8'|c=='9'){
                flag =true;
            }
        }
        return flag;
    }
    /**
     * 验证手机号码
     *
     * 移动号码段:139、138、137、136、135、134、150、151、152、157、158、159、182、183、187、188、147
     * 联通号码段:130、131、132、136、185、186、145
     * 电信号码段:133、153、180、189
     *
     * @param phoneNumber 需要判断的手机号码
     * @return 返回是否是手机号
     */
    public static boolean checkCellphone(String phoneNumber) {
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * 验证本地时间
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
