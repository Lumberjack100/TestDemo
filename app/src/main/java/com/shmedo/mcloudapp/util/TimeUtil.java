package com.shmedo.mcloudapp.util;

import android.text.TextUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   TimeUtil
 * 创建者:   dpc
 * 创建时间:  2019/1/15 17:41
 */
public class TimeUtil {
    private static SimpleDateFormat sdf = null;
    private static SimpleDateFormat sdf_systime_format = new SimpleDateFormat("yyMMddHHmmss");
    private static SimpleDateFormat sdf_systime_format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatUTC(long l, String strPattern) {
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
     *
     * @return
     */
    public static String getSysTimeStr() {
        try {
            return sdf_systime_format.format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            Timber.i("DateUtil.getSysTime():" + e.getMessage());
            return "";
        }

    }

    public static String getCurrentTime() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String getDateBefore(int day) {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return sdf_systime_format2.format(now.getTime());
    }

    public static int sencondBetweenTimestamp(Timestamp begin, Timestamp end) {
        long milli = end.getTime() - begin.getTime();
        return (int) (milli / 1000);
    }
}
