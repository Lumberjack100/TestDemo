package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.OsmometerStatus;

/**
 * Created by adu on 2018/1/19.
 *  查询数字式渗压计参数实体类
 */
public class QueryOsmometerParameterInfo {
    private OsmometerStatus osmometerStatus;
    private String osmometerAddress;
    private int depthTrigger; //深度触发
    private int temperatureTrigger; //温度触发
    private double depthCorrect; //水深度修正
    private double temperatureCorrect; //温度修正
    private double cordLenght;  //渗压计绳长


    public OsmometerStatus getOsmometerStatus() {
        return osmometerStatus;
    }


    public void setOsmometerStatus(OsmometerStatus osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }


    public String getOsmometerAddress() {
        return osmometerAddress;
    }


    public void setOsmometerAddress(String osmometerAddress) {
        this.osmometerAddress = osmometerAddress;
    }


    public int getDepthTrigger() {
        return depthTrigger;
    }


    public void setDepthTrigger(int depthTrigger) {
        this.depthTrigger = depthTrigger;
    }


    public int getTemperatureTrigger() {
        return temperatureTrigger;
    }


    public void setTemperatureTrigger(int temperatureTrigger) {
        this.temperatureTrigger = temperatureTrigger;
    }


    public double getDepthCorrect() {
        return depthCorrect;
    }


    public void setDepthCorrect(double depthCorrect) {
        this.depthCorrect = depthCorrect;
    }


    public double getTemperatureCorrect() {
        return temperatureCorrect;
    }


    public void setTemperatureCorrect(double temperatureCorrect) {
        this.temperatureCorrect = temperatureCorrect;
    }


    public double getCordLenght() {
        return cordLenght;
    }


    public void setCordLenght(double cordLenght) {
        this.cordLenght = cordLenght;
    }


    @Override public String toString() {
        return "QueryOsmometerParameterInfo{" +
            "osmometerStatus=" + osmometerStatus +
            ", osmometerAddress='" + osmometerAddress + '\'' +
            ", depthTrigger=" + depthTrigger +
            ", temperatureTrigger=" + temperatureTrigger +
            ", depthCorrect=" + depthCorrect +
            ", temperatureCorrect=" + temperatureCorrect +
            ", cordLenght=" + cordLenght +
            '}';
    }
}
