package com.shmedo.configlibrary.ble.model;


import android.text.TextUtils;

import com.shmedo.configlibrary.ble.enums.OsmometerStatus;

/**
 * Created by adu on 2018/1/19.
 * 查询数字水位计参数实体类
 */
public class QueryOsmometerParameterInfo {
    private OsmometerStatus osmometerStatus;
    private String osmometerAddress;
    private String depthTrigger; //深度触发
    private String temperatureTrigger; //温度触发
    private String depthCorrect; //水深度修正
    private String temperatureCorrect; //温度修正
    private String cordLenght;  //绳长
    private String installHeight;  //安装高程

    public OsmometerStatus getOsmometerStatus() {
        return osmometerStatus;
    }


    public void setOsmometerStatus(OsmometerStatus osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }


    public String getOsmometerAddress() {
        return TextUtils.isEmpty(osmometerAddress) ? "" : osmometerAddress;
    }


    public void setOsmometerAddress(String osmometerAddress) {
        this.osmometerAddress = osmometerAddress;
    }

    public String getDepthTrigger() {
        return TextUtils.isEmpty(depthTrigger) ? "" : depthTrigger;
    }

    public void setDepthTrigger(String depthTrigger) {
        this.depthTrigger = depthTrigger;
    }

    public String getTemperatureTrigger() {
        return TextUtils.isEmpty(temperatureTrigger) ? "" : temperatureTrigger;
    }

    public void setTemperatureTrigger(String temperatureTrigger) {
        this.temperatureTrigger = temperatureTrigger;
    }

    public String getDepthCorrect() {
        return TextUtils.isEmpty(depthCorrect) ? "" : depthCorrect;
    }

    public void setDepthCorrect(String depthCorrect) {
        this.depthCorrect = depthCorrect;
    }

    public String getTemperatureCorrect() {
        return TextUtils.isEmpty(temperatureCorrect) ? "" : temperatureCorrect;
    }

    public void setTemperatureCorrect(String temperatureCorrect) {
        this.temperatureCorrect = temperatureCorrect;
    }

    public String getCordLenght() {
        return TextUtils.isEmpty(cordLenght) ? "" : cordLenght;
    }

    public void setCordLenght(String cordLenght) {
        this.cordLenght = cordLenght;
    }

    public String getInstallHeight() {
        return TextUtils.isEmpty(installHeight) ? "" : installHeight;
    }

    public void setInstallHeight(String installHeight) {
        this.installHeight = installHeight;
    }

    @Override
    public String toString() {
        return "QueryOsmometerParameterInfo{" +
                "osmometerStatus=" + osmometerStatus +
                ", osmometerAddress='" + osmometerAddress + '\'' +
                ", depthTrigger='" + depthTrigger + '\'' +
                ", temperatureTrigger='" + temperatureTrigger + '\'' +
                ", depthCorrect='" + depthCorrect + '\'' +
                ", temperatureCorrect='" + temperatureCorrect + '\'' +
                ", cordLenght='" + cordLenght + '\'' +
                ", installHeight='" + installHeight + '\'' +
                '}';
    }
}
