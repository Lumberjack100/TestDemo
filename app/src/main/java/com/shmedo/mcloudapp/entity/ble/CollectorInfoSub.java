package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * ProgramName:   das-config-app
 * PackageName:   com.example.medoDas.entity
 * Author:        社会小青年
 * Date:          2018/4/14 15:09
 * Description：
 */

public class CollectorInfoSub {
    private String collectorAddress;    //采集器地址
    private String standbyTime;         //待机时长
    private String workTime;            //解算频度
    private String collectorInterval;   //采集频度
    private int accessSum;              //接入总数

@JavascriptInterface
    public String getCollectorAddress() {
        return collectorAddress;
    }


    public void setCollectorAddress(String collectorAddress) {
        this.collectorAddress = collectorAddress;
    }

    @JavascriptInterface
    public String getStandbyTime() {
        return standbyTime;
    }


    public void setStandbyTime(String standbyTime) {
        this.standbyTime = standbyTime;
    }

    @JavascriptInterface
    public String getWorkTime() {
        return workTime;
    }


    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    @JavascriptInterface
    public String getCollectorInterval() {
        return collectorInterval;
    }


    public void setCollectorInterval(String collectorInterval) {
        this.collectorInterval = collectorInterval;
    }

    @JavascriptInterface
    public int getAccessSum() {
        return accessSum;
    }


    public void setAccessSum(int accessSum) {
        this.accessSum = accessSum;
    }
}
