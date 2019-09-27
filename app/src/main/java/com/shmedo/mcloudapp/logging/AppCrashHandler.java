package com.shmedo.mcloudapp.logging;

import android.content.Context;
import android.os.Looper;

import com.shmedo.mcloudapp.util.ToastUtil;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.logging
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 * 描述：    TODO
 */
public class AppCrashHandler {

    private Context mContext;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private static AppCrashHandler instance;

    public static AppCrashHandler getInstance(Context mContext) {
        if (instance == null) {
            instance = new AppCrashHandler(mContext);
        }

        return instance;
    }


    private AppCrashHandler(Context context) {
        this.mContext = context;
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable ex) {
                Timber.e(ex, "米易通异常退出：" + ex.getMessage());

                //如果用户没有处理则让系统默认的异常处理器来处理
                uncaughtExceptionHandler.uncaughtException(thread, ex);


//                if (!handleException(ex) && uncaughtExceptionHandler != null) {
//                    //如果用户没有处理则让系统默认的异常处理器来处理
//                    uncaughtExceptionHandler.uncaughtException(thread, ex);
//
//                } else {
//
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(1);
//                    System.gc();
//                }
            }
        });
    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtil.showShortToast("很抱歉！米易通出现异常，即将退出。");
                Looper.loop();
            }
        }.start();

        //保存日志文件
//        saveCatchInfo2File(ex);
        return true;
    }


}
