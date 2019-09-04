package com.shmedo.mcloudapp;

import android.app.Application;
import android.content.Context;

import com.shmedo.mcloudapp.logging.AppCrashHandler;
import com.shmedo.mcloudapp.logging.CrashReportingTree;
import com.tencent.mmkv.MMKV;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   APP
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:16
 * 描述：    TODO
 */
public class App extends Application {
    private static App mAppInstance;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInstance = this;
        mContext = getApplicationContext();
        //初始化facebook.stetho
        //AppInit.init(this);
        //初始化蒲公英
        //PgyCrashManager.register(this);
        MMKV.initialize(this);

        //日志输出
        initTimber();

        // crash handler
        AppCrashHandler.getInstance(this);
    }


    /**
     * 设置日志输出
     */
    private void initTimber() {
        Timber.d("initTimber()------in");
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }


    public static Context getContext() {
        return mContext;
    }

    public static App getInstance() {
        return mAppInstance;
    }
}

