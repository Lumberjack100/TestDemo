package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble
 * 文件名:   SystemRunStateSub
 * 创建者:   dpc
 * 创建时间:  2019/3/7 14:06
 * 描述：    TODO
 */
public class SystemRunStateSub {
    private String gprsSignal;//GPRS信号强度
    private String gpsNumber;
    private String systemStartUp;
    private String systemRestart;
    private String simCCID;//SIM卡CCID
    private String internalTemperature;
    private String batteryVoltage;//设备电池电压
    private String operator;   //运营商
    private String networkMode;    //网络模式

    //@JavascriptInterface
    public String getGprsSignal() {
        return gprsSignal;
    }


    public void setGprsSignal(String gprsSignal) {
        this.gprsSignal = gprsSignal;
    }

    //@JavascriptInterface
    public String getGpsNumber() {
        return gpsNumber;
    }


    public void setGpsNumber(String gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    //@JavascriptInterface
    public String getSystemStartUp() {
        return systemStartUp;
    }


    public void setSystemStartUp(String systemStartUp) {
        this.systemStartUp = systemStartUp;
    }

    //@JavascriptInterface
    public String getSystemRestart() {
        return systemRestart;
    }


    public void setSystemRestart(String systemRestart) {
        this.systemRestart = systemRestart;
    }

    //@JavascriptInterface
    public String getSimCCID() {
        return simCCID;
    }


    public void setSimCCID(String simCCID) {
        this.simCCID = simCCID;
    }

    //@JavascriptInterface
    public String getInternalTemperature() {
        return internalTemperature;
    }


    public void setInternalTemperature(String internalTemperature) {
        this.internalTemperature = internalTemperature;
    }

    //@JavascriptInterface
    public String getBatteryVoltage() {
        return batteryVoltage;
    }


    public void setBatteryVoltage(String batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    //@JavascriptInterface
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    //@JavascriptInterface
    public String getNetworkMode() {
        return networkMode;
    }


    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }


    @Override public String toString() {
        return "SystemRunStateSub{" +
            "gprsSignal='" + gprsSignal + '\'' +
            ", gpsNumber='" + gpsNumber + '\'' +
            ", systemStartUp='" + systemStartUp + '\'' +
            ", systemRestart='" + systemRestart + '\'' +
            ", simCCID='" + simCCID + '\'' +
            ", internalTemperature='" + internalTemperature + '\'' +
            ", batteryVoltage='" + batteryVoltage + '\'' +
            ", operator='" + operator + '\'' +
            ", networkMode='" + networkMode + '\'' +
            '}';
    }
}
