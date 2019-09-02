package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.entity
 * 文件名:   RebootDeviceSub
 * 创建者:   dpc
 * 创建时间:  2018/4/14 13:43
 * 描述：    获取重启设备的实体类
 */
public class RebootDeviceSub {
    private int time;

    //@JavascriptInterface
    public int getTime() {
        return time;
    }


    public void setTime(int time) {
        this.time = time;
    }


    @Override public String toString() {
        return "RebootDeviceSub{" +
            "time=" + time +
            '}';
    }
}
