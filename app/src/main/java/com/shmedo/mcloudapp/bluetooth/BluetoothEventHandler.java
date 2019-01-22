package com.shmedo.mcloudapp.bluetooth;

/**
 * Created by Liudongdong on 18/2/1.
 * 注意，此事件处理程序运行在后台线程中
 */

public interface BluetoothEventHandler {
    void handle(BluetoothEvent event);
}
