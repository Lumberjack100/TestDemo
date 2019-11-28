package com.shmedo.mcloudapp.entity.ble.collector;


/**
 * mqtt配置实体类
 */
public class MqttConfigInfoSub {
    private String tvTitle;
    private int communicationProtocol;
    private String cetServiceAddress;
    private int spRegistrationPlatform;
    private String cetAppKey;
    private String cetRegisterAddress;
    private String cetKeepAliveValue;
    private String cetDeviceSn;
    private String cetProductId;
    private String cetRegistrationCode;
    private String cetMqttDeviceId;
    private String cetMqttUsername;
    private String cetMqttPassword;

    public String getTvTitle() {
        return tvTitle;
    }

    public void setTvTitle(String tvTitle) {
        this.tvTitle = tvTitle;
    }

    public int getCommunicationProtocol() {
        return communicationProtocol;
    }

    public void setCommunicationProtocol(int communicationProtocol) {
        this.communicationProtocol = communicationProtocol;
    }

    public String getCetServiceAddress() {
        return cetServiceAddress;
    }

    public void setCetServiceAddress(String cetServiceAddress) {
        this.cetServiceAddress = cetServiceAddress;
    }

    public int getSpRegistrationPlatform() {
        return spRegistrationPlatform;
    }

    public void setSpRegistrationPlatform(int spRegistrationPlatform) {
        this.spRegistrationPlatform = spRegistrationPlatform;
    }

    public String getCetAppKey() {
        return cetAppKey;
    }

    public void setCetAppKey(String cetAppKey) {
        this.cetAppKey = cetAppKey;
    }

    public String getCetRegisterAddress() {
        return cetRegisterAddress;
    }

    public void setCetRegisterAddress(String cetRegisterAddress) {
        this.cetRegisterAddress = cetRegisterAddress;
    }

    public String getCetKeepAliveValue() {
        return cetKeepAliveValue;
    }

    public void setCetKeepAliveValue(String cetKeepAliveValue) {
        this.cetKeepAliveValue = cetKeepAliveValue;
    }

    public String getCetDeviceSn() {
        return cetDeviceSn;
    }

    public void setCetDeviceSn(String cetDeviceSn) {
        this.cetDeviceSn = cetDeviceSn;
    }

    public String getCetProductId() {
        return cetProductId;
    }

    public void setCetProductId(String cetProductId) {
        this.cetProductId = cetProductId;
    }

    public String getCetRegistrationCode() {
        return cetRegistrationCode;
    }

    public void setCetRegistrationCode(String cetRegistrationCode) {
        this.cetRegistrationCode = cetRegistrationCode;
    }

    public String getCetMqttDeviceId() {
        return cetMqttDeviceId;
    }

    public void setCetMqttDeviceId(String cetMqttDeviceId) {
        this.cetMqttDeviceId = cetMqttDeviceId;
    }

    public String getCetMqttUsername() {
        return cetMqttUsername;
    }

    public void setCetMqttUsername(String cetMqttUsername) {
        this.cetMqttUsername = cetMqttUsername;
    }

    public String getCetMqttPassword() {
        return cetMqttPassword;
    }

    public void setCetMqttPassword(String cetMqttPassword) {
        this.cetMqttPassword = cetMqttPassword;
    }
}
