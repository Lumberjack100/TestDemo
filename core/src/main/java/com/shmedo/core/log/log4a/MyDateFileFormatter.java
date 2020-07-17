package com.shmedo.core.log.log4a;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.pqpo.librarylog4a.formatter.Formatter;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/5/8 <br/>
 * 描述：   日志文件输出格式类
 */
public class MyDateFileFormatter implements Formatter {
    private SimpleDateFormat simpleDateFormat = null;
    private Date mDate;
    private String lastDataFormated = null;
    private StringBuffer mStringBuffer;
    private int mTimeLength = 0;

    public MyDateFileFormatter() {
        this("yyyy:MM:dd HH:mm:ss");
    }

    public MyDateFileFormatter(String pattern) {
        simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        mStringBuffer = new StringBuffer();
        //重置秒数
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.SECOND, 0);
        mDate = instance.getTime();
    }

    public synchronized String format(int logLevel, String tag, String msg) {
        if ((System.currentTimeMillis() - mDate.getTime()) > 1000 || lastDataFormated == null) {
            mDate.setTime(System.currentTimeMillis());
            lastDataFormated = simpleDateFormat.format(mDate);
            resetTimePrefix();
            return formatString(logLevel, tag, msg);
        }
        return formatString(logLevel, tag, msg);
    }

    private void resetTimePrefix() {
        if (mStringBuffer.length() > 0) {
            mStringBuffer.delete(0, mStringBuffer.length());
        }
        mTimeLength = mStringBuffer.append(lastDataFormated).append(' ').length();
    }

    private String formatString(int logLevel, String tag, String msg) {
        if (mStringBuffer.length() > mTimeLength) {
            mStringBuffer.delete(mTimeLength, mStringBuffer.length());
        }

//        return mStringBuffer.append(Level.getShortLevelName(logLevel)).append("/").append(tag).append(": ").append(msg).append('\n').toString();
        return mStringBuffer.append(msg).append('\n').toString();
    }
}
