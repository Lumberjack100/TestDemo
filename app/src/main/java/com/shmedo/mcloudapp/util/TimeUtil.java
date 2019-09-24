package com.shmedo.mcloudapp.util;

import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   TimeUtil
 * 创建者:   dpc
 * 创建时间:  2019/1/15 17:41
 * 描述：    TODO
 */
public class TimeUtil {
    private static SimpleDateFormat sdf = null;
    private static SimpleDateFormat sdf_systime_format=new SimpleDateFormat("yyMMddHHmmss");
    public  static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }

    /**
     * 获取系统授时时间格式
     * @return
     */
    public static  String getSysTimeStr(){
        try {
            return sdf_systime_format.format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            Log.i("adu","DateUtil.getSysTime():" + e.getMessage());
            return "";
        }

    }
}
