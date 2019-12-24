package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：    日志文件工具类
 */
public class LogFileUtil {
    private static final String DEFAULT_FORMAT = "yyyy_MM_dd";
    private static final int SYSTEM = 1024;
    private static final int DIRECTORY_SIZE = 10;






    /**
     * 创建 logcat文件
     *
     * @param file file
     * @return File
     */
    public static File createLogFile(File file,String snNumber) {

        if (file.exists()) {//存在
            if (file.isFile()) {
                return createFile(file);
            } else if (file.isDirectory()) {
                return createLogFile(file.getAbsolutePath()+"/mCloudLogFiles/"+snNumber+"/", getFileName(), false);
            }
        }
        else {
            if (file.mkdirs()) {
                return createLogFile(file,snNumber);
            }
        }
        return file;
    }



    /**
     * 创建log文件
     *
     * @param path       path
     * @param fileName   fileName
     * @param cleanCache cleanCache
     */
    private static File createLogFile(String path, String fileName, boolean cleanCache) {
        File directory = new File(path);
        if (!directory.exists()) {

            directory.mkdirs();
        }

        // 是否删除缓存日志文件
//        if (cleanCache) {
//            computeSize(directory);
//        }

        File file = new File(directory, fileName);
        return createFile(file);
    }

    /**
     * 新建文件
     *
     * @param file
     * @return
     */
    @NonNull
    private static File createFile(File file) {
        if (file.exists()) {
            return file;
        }else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }

    }

    /**
     * 获取缓存大小
     *
     * @param directory directory
     */
    private static void computeSize(File directory) {
        long length = 0L;
        if (directory.exists()) {
            for (File file1 : directory.listFiles()) {
                length += file1.length();
            }
        }

        //限定大小 10M
        if ((length / SYSTEM / SYSTEM) >= DIRECTORY_SIZE) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
    }

    /**
     * 文件名
     *
     * @return FileName
     */
    private static String getFileName() {
        DateFormat format = new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault());
        return format.format(new Date(System.currentTimeMillis())) + ".txt";
    }

    /**
     * 文件路径
     *
     * @param context Context
     * @param dirName dirName
     * @return FileDir
     */
    private static String getCacheFileDir(Context context, String dirName) {
        String name = "/" + dirName;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir() + name;
        } else {
            return context.getCacheDir() + name;
        }
    }
}
