package com.shmedo.configlibrary.ble.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    设备状态信息实体类
 */
public class DeviceStatusInfoOne {

    /**
     * $$041,(1),(2),(3),(4),(5),(6)\r\n
     * （1）SN号
     * （2）IMEI号
     * （3）SIM卡号
     * （4）启动代码1
     * （5）启动代码2
     * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号
     * 示例：
     * $$041,150000L,865860047575320,898604061918C0643348,20,1,9
     */
    private String snNumber;
    private String imeiNumber;
    private String simNumber;
    private String startCodeOne;
    private String startCodeTwo;
    private String signalStrength;

    public String getSnNumber() {
        return TextUtils.isEmpty(snNumber) ? "" : snNumber;
    }

    public void setSnNumber(String snNumber) {
        this.snNumber = snNumber;
    }

    public String getImeiNumber() {
        return TextUtils.isEmpty(imeiNumber) ? "" : imeiNumber;
    }

    public void setImeiNumber(String imeiNumber) {
        this.imeiNumber = imeiNumber;
    }

    public String getSimNumber() {
        return TextUtils.isEmpty(simNumber) ? "" : simNumber;
    }

    public void setSimNumber(String simNumber) {
        this.simNumber = simNumber;
    }

    public String getStartCodeOne() {
        return TextUtils.isEmpty(startCodeOne) ? "" : startCodeOne;
    }

    public void setStartCodeOne(String startCodeOne) {
        this.startCodeOne = startCodeOne;
    }

    public String getStartCodeTwo() {
        return TextUtils.isEmpty(startCodeTwo) ? "" : startCodeTwo;
    }

    public void setStartCodeTwo(String startCodeTwo) {
        this.startCodeTwo = startCodeTwo;
    }

    public String getSignalStrength() {
        return TextUtils.isEmpty(signalStrength) ? "" : signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    @Override
    public String toString() {
        return "DeviceStatusInfoOne{" +
                "snNumber='" + snNumber + '\'' +
                ", imeiNumber='" + imeiNumber + '\'' +
                ", simNumber='" + simNumber + '\'' +
                ", startCodeOne='" + startCodeOne + '\'' +
                ", startCodeTwo='" + startCodeTwo + '\'' +
                ", signalStrength='" + signalStrength + '\'' +
                '}';
    }
}
