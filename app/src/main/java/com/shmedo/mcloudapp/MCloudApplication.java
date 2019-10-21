package com.shmedo.mcloudapp;

import android.app.Application;

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
public class MCloudApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MCloudApp.initialize(this);

        //初始化蒲公英
        //PgyCrashManager.register(this);

        AppCrashHandler.getInstance(this);// crash handler

        //基于 mmap 的高性能通用 key-value 组件
        MMKV.initialize(this);

        //日志输出
        initTimber();
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

}

