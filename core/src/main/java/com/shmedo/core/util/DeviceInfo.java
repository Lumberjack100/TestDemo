package com.shmedo.core.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.shmedo.core.MCloudApp;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/8 <br/>
 * 描述：     提供所有与设备相关的信息。
 */
public class DeviceInfo {

    /**
     * 获取当前设备屏幕的宽度，以像素为单位。
     * @return 当前设备屏幕的宽度
     */
    public static int getScreenWidth() {
        WindowManager windowManager = (WindowManager) MCloudApp.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (AndroidVersion.hasJellyBeanMR1()) {
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        return metrics.widthPixels;
    }

    /**
     * 得到屏幕的高度，以像素为单位。
     * @return 当前设备屏幕的高度
     */
    public static int getScreenHeight() {
        WindowManager windowManager = (WindowManager) MCloudApp.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (AndroidVersion.hasJellyBeanMR1()) {
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        return metrics.heightPixels;
    }
}
