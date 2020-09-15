package com.shmedo.configlibrary.ble.model;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   mqtt配置实体类
 */
public class MqttConfigInfo implements Serializable {
    /**
     * $$8893,1,4,mqtt.shmedo.com 6883,300,150000L,150000L,a84b42b1-cb30-410f-8285-5f4de6f9d319,
     * 2,mqtt.shmedo.com 80,fXQQROerSlJ0bqTPCoMnyqgR-2dzhytztk3eYV6nuA0OBQljkqG_exXYtNfr,,,
     * 查询数据中心参数：##889n\r\n，其中 n：表示中心编号，取值1，2，3
     * 返回参数：##889n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)\r\n
     *   (1):数据中心开关，0：关闭，1：打开
     *   (2):通讯协议，2：MDM协议，4：MQTT自动注册，5：MQTT手动注册
     *   (3):数据平台地址
     *   (4):keepAlive
     *   (5):设备SN号
     *   (6):产品ID
     *   (7):注册码
     *   (8):注册平台类型，0：地大平台，1：成都理工平台，2：米度平台
     *   (9):注册平台地址
     *   (9):APPKey
     *   (10):MQTT设备ID
     *   (11):MQTT用户名
     *   (12):MQTT密码
     *   注：不同通讯协议下的参数不一致，不存在的参数，逗号之间为空
     */

    private int dataCenterSwitch;
    private String communicationProtocol;
    private String dataPlatformAddress;
    private String keepAliveValue;
    private String deviceSn;
    private String productId;
    private String registerCode;
    private String registerPlatform;
    private String registerPlatformAddress;
    private String appKey;
    private String mqttDeviceId;
    private String mqttUsername;
    private String mqttPassword;


    public int getDataCenterSwitch() {
        return dataCenterSwitch;
    }

    public void setDataCenterSwitch(int dataCenterSwitch) {
        this.dataCenterSwitch = dataCenterSwitch;
    }

    public String getCommunicationProtocol() {
        return communicationProtocol;
    }

    public void setCommunicationProtocol(String communicationProtocol) {
        this.communicationProtocol = communicationProtocol;
    }

    public String getDataPlatformAddress() {
        return dataPlatformAddress;
    }

    public void setDataPlatformAddress(String dataPlatformAddress) {
        this.dataPlatformAddress = dataPlatformAddress;
    }

    public String getKeepAliveValue() {
        return keepAliveValue;
    }

    public void setKeepAliveValue(String keepAliveValue) {
        this.keepAliveValue = keepAliveValue;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(String registerCode) {
        this.registerCode = registerCode;
    }

    public String getRegisterPlatform() {
        return registerPlatform;
    }

    public void setRegisterPlatform(String registerPlatform) {
        this.registerPlatform = registerPlatform;
    }

    public String getRegisterPlatformAddress() {
        return registerPlatformAddress;
    }

    public void setRegisterPlatformAddress(String registerPlatformAddress) {
        this.registerPlatformAddress = registerPlatformAddress;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getMqttDeviceId() {
        return mqttDeviceId;
    }

    public void setMqttDeviceId(String mqttDeviceId) {
        this.mqttDeviceId = mqttDeviceId;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public void setMqttUsername(String mqttUsername) {
        this.mqttUsername = mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public void setMqttPassword(String mqttPassword) {
        this.mqttPassword = mqttPassword;
    }

    @Override
    public String toString() {
        return "MqttConfigInfo{" +
                "dataCenterSwitch=" + dataCenterSwitch +
                ", communicationProtocol='" + communicationProtocol + '\'' +
                ", dataPlatformAddress='" + dataPlatformAddress + '\'' +
                ", keepAliveValue='" + keepAliveValue + '\'' +
                ", deviceSn='" + deviceSn + '\'' +
                ", productId='" + productId + '\'' +
                ", registrationCode='" + registerCode + '\'' +
                ", registrationPlatform='" + registerPlatform + '\'' +
                ", registrationPlatformAddress='" + registerPlatformAddress + '\'' +
                ", appKey='" + appKey + '\'' +
                ", mqttDeviceId='" + mqttDeviceId + '\'' +
                ", mqttUsername='" + mqttUsername + '\'' +
                ", mqttPassword='" + mqttPassword + '\'' +
                '}';
    }
}
