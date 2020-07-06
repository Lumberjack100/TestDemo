package com.shmedo.mcloudapp.log;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.shmedo.mcloudapp.util.ActivityCollector;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.logging
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 *
 */
public class AppCrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private static AppCrashHandler instance;

    public static AppCrashHandler getInstance(Context mContext) {
        if (instance == null) {
            synchronized (AppCrashHandler.class) {
                if (instance == null) {
                    instance = new AppCrashHandler(mContext);
                }
            }
        }

        return instance;
    }


    private AppCrashHandler(Context context) {
        this.mContext = context;
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    /**
     * 当程序中有未被捕获的异常，系统将会自动调用uncaughtException方法
     *
     * @param t  出现未捕获异常的线程
     * @param ex 未捕获的异常，有了这个ex，我们就可以得到异常信息
     */
    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        Timber.e(ex, " mCloud Device异常退出：" + ex.getMessage());

        killProcess();

//        if (!handleException(ex) && uncaughtExceptionHandler != null) {
//            //如果用户没有处理则让系统默认的异常处理器来处理
//            uncaughtExceptionHandler.uncaughtException(thread, ex);
//
//        } else {
//
//        killProcess();
//        }
    }


    /**
     * 退出应用
     */
    public void killProcess() {
        //结束应用
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "哎呀，程序发生异常啦...", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();

        ActivityCollector.finishAll();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Timber.e("CrashHandler.InterruptedException--->" + ex.toString());
        }
        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
