package com.shmedo.core.log.log4a;

import android.content.Context;

import com.shmedo.core.util.LogFileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.pqpo.librarylog4a.Level;
import me.pqpo.librarylog4a.Log4a;
import me.pqpo.librarylog4a.LogData;
import me.pqpo.librarylog4a.appender.AndroidAppender;
import me.pqpo.librarylog4a.appender.FileAppender;
import me.pqpo.librarylog4a.interceptor.Interceptor;
import me.pqpo.librarylog4a.logger.AppenderLogger;

/**
 * Created by pqpo on 2017/11/24.
 */
public class LogInit {

    private static final int BUFFER_SIZE = 1024 * 400; //400k

    private static final String DEFAULT_FORMAT = "yyyy_MM_dd";


    public static void init(Context context) {
        int level = Level.DEBUG;
        Interceptor wrapInterceptor = new Interceptor() {
            @Override
            public boolean intercept(LogData logData) {
//                logData.tag = "Log4a-" + logData.tag;
                return true;
            }
        };
        AndroidAppender androidAppender = new AndroidAppender.Builder()
                .setLevel(level)
                .addInterceptor(wrapInterceptor)
                .create();

        File logDir = LogFileUtil.INSTANCE.getLogDir(context);
        String buffer_path = logDir.getAbsolutePath() + File.separator + ".logCache";
        String log_path = logDir.getAbsolutePath() + File.separator + getCurrentDate() + ".txt";
        FileAppender fileAppender = new FileAppender.Builder(context)
                .setLogFilePath(log_path)
                .setLevel(level)
                .addInterceptor(wrapInterceptor)
                .setBufferFilePath(buffer_path)
                .setFormatter(new MyDateFileFormatter())
                .setCompress(false)
                .setBufferSize(BUFFER_SIZE)
                .create();

        AppenderLogger logger = new AppenderLogger.Builder()
                .addAppender(androidAppender)
                .addAppender(fileAppender)
                .create();
        Log4a.setLogger(logger);
    }

    /**
     * 文件名
     *
     * @return FileName
     */
    private static String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault());
        return format.format(new Date(System.currentTimeMillis()));
    }

}
