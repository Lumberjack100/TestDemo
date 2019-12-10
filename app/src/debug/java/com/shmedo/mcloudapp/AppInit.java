package com.shmedo.mcloudapp;
import android.content.Context;
import com.facebook.stetho.Stetho;

/**
 * 项目名：  mobileAndroid
 * 包名：    com.shmedo.mobileandroid
 * 文件名:   AppInit
 * 创建者:   dpc
 * 创建时间: 2018/11/25 12:54
 *
 */

public class AppInit{
    public static final boolean DEG = true;
    public static void init(Context context){
        if (DEG){
            Stetho.initializeWithDefaults(context);
        }

    }
}
