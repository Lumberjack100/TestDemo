package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/18 <br/>
 * 描述：     设备概要信息接口返回实体
 */
public class DeviceSimpleInfo {

    private int id;
    private int companyID;
    private int productID;
    private String productType;
    private String deviceToken;
    private String deviceName;
    private Object parentID;
    private Object installLocation;
    private String gpsLocation;
    private String deviceStatus;
    private String apiKey;
    private String firmwareVersion;
    private Object exValues;
    private String uniqueToken;
    private boolean allowWarn;
    private int warnInterval;
    private String productToken;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Object getParentID() {
        return parentID;
    }

    public void setParentID(Object parentID) {
        this.parentID = parentID;
    }

    public Object getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(Object installLocation) {
        this.installLocation = installLocation;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Object getExValues() {
        return exValues;
    }

    public void setExValues(Object exValues) {
        this.exValues = exValues;
    }

    public String getUniqueToken() {
        return uniqueToken;
    }

    public void setUniqueToken(String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public boolean isAllowWarn() {
        return allowWarn;
    }

    public void setAllowWarn(boolean allowWarn) {
        this.allowWarn = allowWarn;
    }

    public int getWarnInterval() {
        return warnInterval;
    }

    public void setWarnInterval(int warnInterval) {
        this.warnInterval = warnInterval;
    }

    public String getProductToken() {
        return productToken;
    }

    public void setProductToken(String productToken) {
        this.productToken = productToken;
    }
}
