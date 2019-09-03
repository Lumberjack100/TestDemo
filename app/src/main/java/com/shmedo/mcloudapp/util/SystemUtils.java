package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 * 描述：    系统工具类
 */
public class SystemUtils
{
    /**
     *
     * 版本名
     */
    public static String getVersionName(Context context)
    {
        return getPackageInfo(context).versionName;
    }


    /**
     *
     * 版本号
     */
    public static int getVersionCode(Context context)
    {
        return getPackageInfo(context).versionCode;
    }

    /**
     * 获取包信息
     */
    public static PackageInfo getPackageInfo(Context context)
    {
        PackageInfo pi = null;

        try
        {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);

            return pi;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return pi;
    }
}
