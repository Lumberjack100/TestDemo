package com.shmedo.mcloudapp.util.bleutil;

import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Liudongdong on 18/2/5.
 */

public class BleHelpUtil {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static int sencondBetweenTimestamp(Timestamp begin, Timestamp end) {
        long milli = end.getTime() - begin.getTime();
        return (int) (milli / 1000);
    }
}
