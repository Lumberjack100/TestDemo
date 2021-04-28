package com.shmedo.mcloudapp.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有时间按当前2014-12-02计算
 * 日期处理工具类
 */
public class DateUtil {
    private static String ymdhms = "yyyy-MM-dd HH:mm:ss";
    private static String ymd = "yyyy-MM-dd";
    private static String year = "yyyy";
    private static String month = "MM";
    private static String day = "dd";
    private static String hm = "HH:mm";
    private static String ymd2 = "yyyy年MM月dd日";

    public static SimpleDateFormat sf_yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);
    public static SimpleDateFormat sf_yyyyMMdd = new SimpleDateFormat(ymd);
    public static SimpleDateFormat sf_year = new SimpleDateFormat(year);
    public static SimpleDateFormat sf_month = new SimpleDateFormat(month);
    public static SimpleDateFormat sf_day = new SimpleDateFormat(day);
    public static SimpleDateFormat sf_hm = new SimpleDateFormat(hm);
    public static SimpleDateFormat sf_yyyyMMdd2 = new SimpleDateFormat(ymd2);

    public static long DATEMM = 86400L;


    /**
     * 默认日期格式
     */
    public static String DEFAULT_FORMAT = "yyyy.MM.dd";

    /**
     * 获得当前时间
     * <p/>
     * 格式：2014-12-02 10:38:53
     *
     * @return String
     */
    public static String getNowDateString() {
        return sf_yyyyMMddHHmmss.format(new Date());
    }

    /**
     * 获取当前时间
     * <p/>
     * 格式：2014-12-02
     *
     * @return String
     */
    public static String getNowDateYYYYMMDDString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * 获得当前时间
     * <p/>
     * 格式：10:38:53
     *
     * @return
     */
    public static String getNowDateHHmmssString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获得当前时间
     * <p/>
     * 格式：10:38
     *
     * @return
     */
    public static String getNowDateHHmmString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * 获取指定格式的当前时间
     * <p/>
     *
     * @param toFormatter 格式字符串，如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getNowStrDate(String toFormatter) {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat(toFormatter);
        return formatter.format(date);
    }


    /**
     * 获取后退或增加N天的日期
     * <br/>
     * 格式：按当前2014-12-02计算，传入2，得到2014-11-30
     *
     * @param date    日期对象
     * @param backDay 整数，可以为负数
     * @return String 返回yyyy-MM-dd格式日期
     */
    public static String getBackOrAddDate(Date date, int backDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, backDay);
        String back = sf_yyyyMMdd.format(calendar.getTime());
        return back;
    }

    /**
     * 获取后退或增加N天的日期
     * <br/>
     *
     * @param date    日期对象
     * @param backDay 整数，可以为负数
     * @return Date日期
     */
    public static Date getBackOrAddDate2(Date date, int backDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, backDay);

        return calendar.getTime();
    }

    /**
     * 将字符串型日期从一种格式转换为另一种格式字符串型日期
     * <p/>
     *
     * @param strDate       待转换的Date类型日期
     * @param fromFormatter 原格式,如：yyyy-MM-dd HH:mm:ss
     * @param toFormatter   新格式,如：yyyy-MM-dd
     * @return String toFormatter
     */
    public static String StrToStrFormat(String strDate, String fromFormatter, String toFormatter) {
        SimpleDateFormat formatter = new SimpleDateFormat(fromFormatter);
        SimpleDateFormat sf = new SimpleDateFormat(toFormatter);
        String date = "";
        try {
            date = sf.format(formatter.parse(strDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将Date类型日期按照给定的格式转换为字符串型日期
     * <p/>
     *
     * @param date        待转换的Date类型日期
     * @param toFormatter 新格式,如：yyyy-MM-dd
     * @return String toFormatter
     */
    public static String DateToStrFormat(Date date, String toFormatter) {
        SimpleDateFormat formatter = new SimpleDateFormat(toFormatter);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 将字符串型日期按照给定的格式转换为Date类型日期
     *
     * @param date   待转换的字符串型日期；
     * @param format 转化的日期格式
     * @return 返回该字符串的日期型数据；
     */
    public static Date stringToDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(date);

        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将字符串型日期转换为Date类型日期
     *
     * @param date 待转换的字符串型日期；
     * @return 返回该字符串的日期型数据，格式为 yyyy-MM-dd
     */
    public static Date stringToDateYYYYMMDD(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (date.contains("年")) {
                date = date.replace("年", "-").replace("月", "-").replace("日", "");
            }

            return sdf.parse(date);

        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将字符串型日期转换为 yyyy-MM-dd 格式日期
     *
     * @param date 待转换的字符串型日期；
     * @return 返回字符串格式为 yyyy-MM-dd
     */
    public static String stringToStringYYYYMMDD(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = "";

        try {
            if (date.contains("年")) {
                date = date.replace("年", "-").replace("月", "-").replace("日", "");
            }

            dateStr = sdf.format(formatter.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }


    /**
     * 获取指定日期所在的年
     *
     * @return String  格式：yyyy
     */
    public static String getCurrentYear(Date date) {
        return sf_year.format(date);
    }

    /**
     * 获取指定日期所在的月
     *
     * @return String  MM
     */
    public static String getCurrentMonth(Date date) {
        return sf_month.format(date);
    }

    /**
     * 获取指定日期所在的日
     *
     * @return String  dd
     */
    public static String getCurrentDay(Date date) {
        return sf_day.format(date);
    }


    /**
     * 获得今天零点的时间
     *
     * @return Date
     */
    public static Date getTodayZeroHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        return cal.getTime();
    }

    /**
     * 获得昨天23时59分59秒的时间
     *
     * @return Date
     */
    public static Date getYesterDay24Hour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.HOUR, 23);
        return cal.getTime();
    }


    /**
     * 把long型日期按照指定的格式转为字符串型日期
     *
     * @param date   long型日期(单位s)
     * @param format 日期格式
     * @return String
     */
    public static String longToString(long date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // 前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        java.util.Date dt2 = new Date(date * 1000L);
        String sDateTime = sdf.format(dt2); // 得到精确到秒的表示：08/31/2006 21:08:00
        return sDateTime;
    }

    /**
     * 把字符串型日期按照指定的格式转为long型日期
     * <br/>返回的结果单位是秒
     *
     * @param date   String 型日期
     * @param format 日期格式；
     * @return long
     */
    public static long stringToLong(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date dt2 = null;
        long lTime = 0;
        try {
            dt2 = sdf.parse(date);
            // 继续转换得到秒数的long型
            lTime = dt2.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lTime;
    }

    /**
     * 获得当前日期与本周一相差的天数
     */
    public static int getMondayPlus(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == 1) {
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }


    /**
     * 获得指定日期所在的自然周的第一天，即周一
     *
     * @param date 日期
     * @return 自然周的第一天，Date型日期
     */
    public static Date getStartDayOfWeek(Date date) {
        int mondayPlus = getMondayPlus(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, mondayPlus);

        date = c.getTime();
        return date;
    }


    /**
     * 获得指定日期所在的自然周的第一天，即周一
     *
     * @param date 日期
     * @return 自然周的第一天，字符串型日期
     */
    public static String getStartDayOfWeekString(Date date) {
        int mondayPlus = getMondayPlus(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, mondayPlus);

        String back = sf_yyyyMMdd.format(c.getTime());
        return back;
    }

    /**
     * 获得指定日期所在的自然周的最后一天，即周日
     *
     * @param date 日期
     * @return 字符串型日期
     */
    public static String getLastDayOfWeek(Date date) {
        int mondayPlus = getMondayPlus(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, mondayPlus + 6);

        String back = sf_yyyyMMdd.format(c.getTime());
        return back;
    }

    /**
     * 获得指定日期所在当月第一天
     *
     * @param date 日期
     * @return Date型日期
     */
    public static Date getStartDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期所在当月最后一天
     *
     * @param date 日期
     * @return Date型日期
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1);
        date = c.getTime();
        return date;
    }

    /**
     * 获取某月的最后一天
     *
     * @throws
     * @Title:getLastDayOfMonth
     * @Description:
     * @param:@param year
     * @param:@param month
     * @param:@return
     * @return:String
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());

        return lastDayOfMonth;
    }

    /**
     * 获得指定日期的下一个月的第一天
     *
     * @param date
     * @return
     */
    public static Date getStartDayOfNextMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return date;
    }

    /**
     * 获得指定日期的下一个月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfNextMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.MONTH, 2);
        c.add(Calendar.DATE, -1);
        date = c.getTime();
        return date;
    }

    /**
     * 求某一个时间向前多少秒的时间(currentTimeToBefer)
     *
     * @param givedTime        给定的时间
     * @param interval         间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)
     * @param format_Date_Sign 输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；
     */
    public static String givedTimeToBefer(String givedTime, long interval, String format_Date_Sign) {
        String tomorrow = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);
            Date gDate = sdf.parse(givedTime);
            long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒
            long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒
            Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2
            tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tomorrow;
    }


    /**
     * 得到二个日期间的间隔日期；
     *
     * @param endTime   结束时间
     * @param beginTime 开始时间
     * @param isEndTime 是否包含结束日期；
     * @return
     */
    public static Map<String, String> getTwoDay(String endTime, String beginTime, boolean isEndTime) {
        Map<String, String> result = new HashMap<String, String>();
        if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime.equals(""))))
            return null;
        try {
            java.util.Date date = sf_yyyyMMdd.parse(endTime);
            endTime = sf_yyyyMMdd.format(date);
            java.util.Date mydate = sf_yyyyMMdd.parse(beginTime);
            long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
            result = getDate(endTime, Integer.parseInt(day + ""), isEndTime);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 得到二个日期间的间隔天数
     *
     * @param endTime   结束时间
     * @param beginTime 开始时间
     * @return
     */
    public static Integer getTwoDayInterval(String endTime, String beginTime) {
        if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime.equals(""))))
            return 0;

        long day = 0l;
        try {
            java.util.Date endDate = sf_yyyyMMdd.parse(endTime);
            java.util.Date beginDate = sf_yyyyMMdd.parse(beginTime);
            day = (endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            return 0;
        }
        return Integer.parseInt(day + "");
    }

    /**
     * 根据结束时间以及间隔差值，求符合要求的日期集合；
     *
     * @param endTime
     * @param interval
     * @param isEndTime
     * @return
     */
    public static Map<String, String> getDate(String endTime, Integer interval, boolean isEndTime) {
        Map<String, String> result = new HashMap<String, String>();
        if (interval == 0 || isEndTime) {
            if (isEndTime)
                result.put(endTime, endTime);
        }
        if (interval > 0) {
            int begin = 0;
            for (int i = begin; i < interval; i++) {
                endTime = givedTimeToBefer(endTime, DATEMM, ymd);
                result.put(endTime, endTime);
            }
        }
        return result;
    }

    /**
     * 计算指定日期加上addDay天后的日期和周几
     *
     * @param dateStr     指定日期字符串
     * @param addDay      加上的天数
     * @param toFormatter 输出格式
     * @return
     */
    public static String getDateWeekAfterOneDateAddDays(String dateStr, int addDay, String toFormatter) {
        String dateWeek = "";

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(stringToDateYYYYMMDD(dateStr));
            calendar.add(Calendar.DATE, addDay);

            SimpleDateFormat sdf = new SimpleDateFormat(toFormatter);

            dateWeek = sdf.format(calendar.getTime()) + " " + getWeek(calendar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateWeek;
    }


    /**
     * 计算当前日期加上addDay天后的日期和周几
     *
     * @param addDay
     * @param toFormatter
     * @return
     */
    public static String getDateWeekAfterNowDateAddDays(int addDay, String toFormatter) {
        String dateWeek = "";

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, addDay);

            SimpleDateFormat sdf = new SimpleDateFormat(toFormatter);

            dateWeek = sdf.format(calendar.getTime()) + " " + getWeek(calendar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateWeek;
    }

    /**
     * 计算当前日期加上addDay天后的日期
     *
     * @param addDay
     * @param toFormatter
     * @return
     */
    public static String getDateAfterNowDateAddDays(int addDay, String toFormatter) {
        String date = "";

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, addDay);

            SimpleDateFormat sdf = new SimpleDateFormat(toFormatter);

            date = sdf.format(calendar.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }


    /**
     * 获取startDay往后N天的日期（年月日周几）
     * endDay 几天
     */
    public static List<String> getSomeDays(int startDay, int endDay) {
        List<String> dates = new ArrayList<String>();

        for (int i = startDay; i <= (startDay + endDay); i++) {
            dates.add(getDateWeekAfterNowDateAddDays(i, "yyyy年MM月dd日"));
        }
        return dates;
    }

    /**
     * 根据当前日期获得是星期几
     *
     * @return
     */
    public static String getWeek(Calendar c) {
        String Week = "";

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            Week += "周日";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            Week += "周一";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            Week += "周二";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            Week += "周三";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            Week += "周四";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            Week += "周五";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            Week += "周六";
        }
        return Week;
    }

    /**
     * 根据输入的日期，获得周几
     *
     * @return
     */
    public static String getWeek(String day, String dateFormat) {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        Date date = null;
        try {
            date = sdf.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();

        c.setTime(date);

        String Week = "";

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            Week += "周日";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            Week += "周一";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            Week += "周二";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            Week += "周三";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            Week += "周四";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            Week += "周五";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            Week += "周六";
        }
        return Week;
    }

    public static int getDayOfWeek(String week) {
        int dayOfWeek = -1;

        switch (week) {
            case "周日":

                dayOfWeek = 1;
                break;

            case "周一":

                dayOfWeek = 2;
                break;

            case "周二":

                dayOfWeek = 3;
                break;

            case "周三":

                dayOfWeek = 4;
                break;

            case "周四":

                dayOfWeek = 5;
                break;

            case "周五":

                dayOfWeek = 6;
                break;

            case "周六":

                dayOfWeek = 7;
                break;
        }

        return dayOfWeek;
    }


    /**
     * 格式化日期
     *
     * @param date 日期对象
     * @return String 日期字符串
     */
    public static String formatDate(Date date) {
        SimpleDateFormat f = new SimpleDateFormat(DEFAULT_FORMAT);
        String sDate = f.format(date);
        return sDate;
    }

    /**
     * 获取当年的第一天
     *
     * @return
     */
    public static Date getCurrYearFirst() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearFirst(currentYear);
    }

    /**
     * 获取当年的最后一天
     *
     * @return
     */
    public static Date getCurrYearLast() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearLast(currentYear);
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currYearLast = calendar.getTime();

        return currYearLast;
    }

    /**
     * 获取某年6月日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getJune(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 6);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currJune = calendar.getTime();
        return currJune;
    }


    /**
     * 根据出生日期获取周岁年龄
     *
     * @param date yyyy-MM-dd 格式
     * @return 年龄
     */
    public static int getCurrentAgeByBirthDay(String date) {
        if (TextUtils.isEmpty(date) || !date.contains("-"))
            return -1;

        Date birthday = stringToDateYYYYMMDD(date);

        //当前时间
        Calendar curr = Calendar.getInstance();

        //生日
        Calendar born = Calendar.getInstance();
        born.setTime(birthday);
//        born.add(Calendar.DATE, 1);

        int age = curr.get(Calendar.YEAR) - born.get(Calendar.YEAR);
        if (age <= 0) {
            return 0;
        }

        //如果当前月份小于生日月份，年龄age需减1；
        //如果月份相等并且当前日小于等于出生日，年龄age需减1；
        int currMonth = curr.get(Calendar.MONTH);
        int currDay = curr.get(Calendar.DAY_OF_MONTH);
        int bornMonth = born.get(Calendar.MONTH);
        int bornDay = born.get(Calendar.DAY_OF_MONTH);


        if ((currMonth < bornMonth) || (currMonth == bornMonth && currDay < bornDay)) {
            age--;
        }

        return age;
    }

}
