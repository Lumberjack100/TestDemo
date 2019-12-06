package com.shmedo.mcloudapp.util;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   gonghe
 * 创建时间:  2019-10-21
 *
 */
public class ActivityCollector {

    private static ArrayList<WeakReference<Activity>> activityList = new ArrayList<WeakReference<Activity>>();

    public static int size() {
        return activityList.size();
    }


    public static void add(WeakReference<Activity> weakRefActivity) {

        if (weakRefActivity != null) {
            activityList.add(weakRefActivity);
        }
    }

    public static void remove(WeakReference<Activity> weakRefActivity) {
        if (weakRefActivity != null) {
            boolean result = activityList.remove(weakRefActivity);
            Timber.d("remove activity reference " + result);
        }
    }

    public static void finishAll() {
        if (!activityList.isEmpty()) {
            for (WeakReference<Activity> weakRefActivity : activityList) {
                Activity activity = weakRefActivity != null ? weakRefActivity.get() : null;
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
            activityList.clear();
        }
    }
}
