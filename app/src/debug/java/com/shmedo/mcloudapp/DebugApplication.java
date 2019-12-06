package com.shmedo.mcloudapp;

import android.content.Context;
import android.os.SystemClock;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 创建者:   gonghe
 * 创建时间:  2019-09-16
 *
 */
public class DebugApplication extends MCloudApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        long startTime = SystemClock.elapsedRealtime();
        initializeStetho(this);
        long elapsed = SystemClock.elapsedRealtime() - startTime;

        Timber.i("Stetho initialized in " + elapsed + " ms");
    }

    /**
     * 初始化facebook.stetho,Stetho is a debug bridge for Android applications,
     * enabling the powerful Chrome Developer Tools and much more.
     * @param context
     */
    private void initializeStetho(final Context context) {
        Stetho.initializeWithDefaults(context);

    }
}
