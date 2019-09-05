package com.shmedo.mcloudapp;

import android.content.Context;
import com.facebook.stetho.Stetho;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 文件名:   AppInit
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:11
 * 描述：    TODO
 */
public class AppInit {
    public static void init(Context context){
        if (BuildConfig.DEBUG){
            Stetho.initializeWithDefaults(context);
        }
    }
}
