package com.shmedo.mcloudapp.logging;

import android.content.Context;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.logging
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 * 描述：    TODO
 */
public class AppCrashHandler {

    private Context context;

    private static AppCrashHandler instance;

    private AppCrashHandler(Context context) {
        this.context = context;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable ex) {
                Timber.e("异常退出：" + ex);

//                CrashReport.postCatchedException(ex);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                System.gc();
            }
        });
    }

    public static AppCrashHandler getInstance(Context mContext) {
        if (instance == null) {
            instance = new AppCrashHandler(mContext);
        }

        return instance;
    }


}
