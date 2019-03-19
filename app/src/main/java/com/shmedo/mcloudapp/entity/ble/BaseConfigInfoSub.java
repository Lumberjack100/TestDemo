package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * ProgramName:   das-config-app
 * PackageName:   com.example.medoDas.entity
 * Author:        社会小青年
 * Date:          2018/1/24 14:26
 * Description：  基础配置信息的Ui展示信息类
 */

public class BaseConfigInfoSub {
    private String token; //设备识别码
    private String localBGNum;      //本地北斗卡号
    private String targetBGNum;     //目标北斗卡号
    private String equipmentStatus;    //设备状态
    private String dataCommunicateMode;     //数据通讯模式
    private String rainfallStation;    //雨量站
    private String collectorModel;  //采集器型号
    private String dataReportInterval; //数据上报间隔
    private String debugModel;      //调试模式
    private String sensorInterfaceType;    //传感器接口类型
    private String serverAddressOne;
    private String serverAddressTwo;
    private String simChoose; //SIM选择

    //@JavascriptInterface
    public String getEquipmentStatus() {
        return equipmentStatus;
    }


    public void setEquipmentStatus(String equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    //@JavascriptInterface
    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }

    //@JavascriptInterface
    public String getLocalBGNum() {
        return localBGNum;
    }


    public void setLocalBGNum(String localBGNum) {
        this.localBGNum = localBGNum;
    }

    //@JavascriptInterface
    public String getTargetBGNum() {
        return targetBGNum;
    }


    public void setTargetBGNum(String targetBGNum) {
        this.targetBGNum = targetBGNum;
    }


    //@JavascriptInterface
    public String getDataCommunicateMode() {
        return dataCommunicateMode;
    }


    public void setDataCommunicateMode(String dataCommunicateMode) {
        this.dataCommunicateMode = dataCommunicateMode;
    }

    //@JavascriptInterface
    public String getRainfallStation() {
        return rainfallStation;
    }


    public void setRainfallStation(String rainfallStation) {
        this.rainfallStation = rainfallStation;
    }

    //@JavascriptInterface
    public String getCollectorModel() {
        return collectorModel;
    }


    public void setCollectorModel(String collectorModel) {
        this.collectorModel = collectorModel;
    }

    //@JavascriptInterface
    public String getDataReportInterval() {
        return dataReportInterval;
    }


    public void setDataReportInterval(String dataReportInterval) {
        this.dataReportInterval = dataReportInterval;
    }

    //@JavascriptInterface
    public String getDebugModel() {
        return debugModel;
    }


    public void setDebugModel(String debugModel) {
        this.debugModel = debugModel;
    }

    //@JavascriptInterface
    public String getSensorInterfaceType() {
        return sensorInterfaceType;
    }


    public void setSensorInterfaceType(String sensorInterfaceType) {
        this.sensorInterfaceType = sensorInterfaceType;
    }

    //@JavascriptInterface
    public String getServerAddressOne() {
        return serverAddressOne;
    }


    public void setServerAddressOne(String serverAddressOne) {
        this.serverAddressOne = serverAddressOne;
    }

//@JavascriptInterface
    public String getServerAddressTwo() {
        return serverAddressTwo;
    }


    public void setServerAddressTwo(String serverAddressTwo) {
        this.serverAddressTwo = serverAddressTwo;
    }


    public String getSimChoose() {
        return simChoose;
    }


    public void setSimChoose(String simChoose) {
        this.simChoose = simChoose;
    }


    @Override public String toString() {
        return "BaseConfigInfoSub{" +
            "token='" + token + '\'' +
            ", localBGNum='" + localBGNum + '\'' +
            ", targetBGNum='" + targetBGNum + '\'' +
            ", equipmentStatus='" + equipmentStatus + '\'' +
            ", dataCommunicateMode='" + dataCommunicateMode + '\'' +
            ", rainfallStation='" + rainfallStation + '\'' +
            ", collectorModel='" + collectorModel + '\'' +
            ", dataReportInterval='" + dataReportInterval + '\'' +
            ", debugModel='" + debugModel + '\'' +
            ", sensorInterfaceType='" + sensorInterfaceType + '\'' +
            ", serverAddressOne='" + serverAddressOne + '\'' +
            ", serverAddressTwo='" + serverAddressTwo + '\'' +
            ", simChoose='" + simChoose + '\'' +
            '}';
    }
}
