package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * ProgramName:   das-config-app
 * PackageName:   com.example.medoDas.entity
 * Author:        社会小青年
 * Date:          2018/3/23 11:13
 * Description：
 */

public class QueryOsmometerParameterSubInfo {
    private String osmometerStatus;
    private String osmometerAddress;
    private int depthTrigger; //深度触发
    private int temperatureTrigger; //温度触发
    private double depthCorrect; //深度修正
    private double temperatureCorrect; //温度修正
    private double syCordLength;//绳长


    @JavascriptInterface
    public String getOsmometerStatus() {
        return osmometerStatus;
    }


    public void setOsmometerStatus(String osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }

    @JavascriptInterface
    public String getOsmometerAddress() {
        return osmometerAddress;
    }


    public void setOsmometerAddress(String osmometerAddress) {
        this.osmometerAddress = osmometerAddress;
    }

    @JavascriptInterface
    public int getDepthTrigger() {
        return depthTrigger;
    }


    public void setDepthTrigger(int depthTrigger) {
        this.depthTrigger = depthTrigger;
    }

    @JavascriptInterface
    public int getTemperatureTrigger() {
        return temperatureTrigger;
    }


    public void setTemperatureTrigger(int temperatureTrigger) {
        this.temperatureTrigger = temperatureTrigger;
    }

    @JavascriptInterface
    public double getDepthCorrect() {
        return depthCorrect;
    }


    public void setDepthCorrect(double depthCorrect) {
        this.depthCorrect = depthCorrect;
    }

    @JavascriptInterface
    public double getTemperatureCorrect() {
        return temperatureCorrect;
    }


    public void setTemperatureCorrect(double temperatureCorrect) {
        this.temperatureCorrect = temperatureCorrect;
    }

    @JavascriptInterface
    public double getSyCordLength() {
        return syCordLength;
    }


    public void setSyCordLength(double syCordLength) {
        this.syCordLength = syCordLength;
    }
}
