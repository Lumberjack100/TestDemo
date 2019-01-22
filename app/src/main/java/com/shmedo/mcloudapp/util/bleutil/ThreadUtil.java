package com.shmedo.mcloudapp.util.bleutil;

import android.os.Looper;

/**
 * Created by Liudongdong on 18/1/31.
 */

public class ThreadUtil {
    public static void checkRunOnUiThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("需要运行在UI线程中");
        }
    }
}
