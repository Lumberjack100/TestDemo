package com.shmedo.mcloudapp.deviceconfig.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：     查询设备列表接口返回的设备信息实体
 */
public class DeviceInfo implements Parcelable {
    private int id;
    private int companyID;
    private String companyName;
    private String deviceToken;
    private String deviceName;
    private String deviceDesc;
    private String installLocation;
    private String gpsLocation;
    private boolean onlineStatus;
    private String deviceStatus;
    private boolean allowWarn;
    private String exValues;
    private String apiKey;
    private int productID;
    private String productToken;
    private String productName;
    private String productType;
    private String firmwareVersion;
    private String lastActiveTime;
    private List<SimListInfo> simList;


    protected DeviceInfo(Parcel in) {
        id = in.readInt();
        companyID = in.readInt();
        companyName = in.readString();
        deviceToken = in.readString();
        deviceName = in.readString();
        deviceDesc = in.readString();
        installLocation = in.readString();
        gpsLocation = in.readString();
        onlineStatus = in.readByte() != 0;
        deviceStatus = in.readString();
        allowWarn = in.readByte() != 0;
        exValues = in.readString();
        apiKey = in.readString();
        productID = in.readInt();
        productToken = in.readString();
        productName = in.readString();
        productType = in.readString();
        firmwareVersion = in.readString();
        lastActiveTime = in.readString();
        simList = in.createTypedArrayList(SimListInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(companyID);
        dest.writeString(companyName);
        dest.writeString(deviceToken);
        dest.writeString(deviceName);
        dest.writeString(deviceDesc);
        dest.writeString(installLocation);
        dest.writeString(gpsLocation);
        dest.writeByte((byte) (onlineStatus ? 1 : 0));
        dest.writeString(deviceStatus);
        dest.writeByte((byte) (allowWarn ? 1 : 0));
        dest.writeString(exValues);
        dest.writeString(apiKey);
        dest.writeInt(productID);
        dest.writeString(productToken);
        dest.writeString(productName);
        dest.writeString(productType);
        dest.writeString(firmwareVersion);
        dest.writeString(lastActiveTime);
        dest.writeTypedList(simList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

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

    public String getCompanyName() {
        return TextUtils.isEmpty(companyName) ? "" : companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDeviceToken() {
        return TextUtils.isEmpty(deviceToken) ? "" : deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceName() {
        return TextUtils.isEmpty(deviceName) ? "" : deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceDesc() {
        return TextUtils.isEmpty(deviceDesc) ? "" : deviceDesc;
    }

    public void setDeviceDesc(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    public String getInstallLocation() {
        return TextUtils.isEmpty(installLocation) ? "" : installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getDeviceStatus() {
        return TextUtils.isEmpty(deviceStatus) ? "" : deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public boolean isAllowWarn() {
        return allowWarn;
    }

    public void setAllowWarn(boolean allowWarn) {
        this.allowWarn = allowWarn;
    }

    public String getExValues() {
        return exValues;
    }

    public void setExValues(String exValues) {
        this.exValues = exValues;
    }

    public String getApiKey() {
        return TextUtils.isEmpty(apiKey) ? "" : apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductToken() {
        return TextUtils.isEmpty(productToken) ? "" : productToken;
    }

    public void setProductToken(String productToken) {
        this.productToken = productToken;
    }

    public String getProductName() {
        return TextUtils.isEmpty(productName) ? "" : productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getFirmwareVersion() {
        return TextUtils.isEmpty(firmwareVersion) ? "" : firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getLastActiveTime() {
        return TextUtils.isEmpty(lastActiveTime) ? "" : lastActiveTime;
    }

    public void setLastActiveTime(String lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public List<SimListInfo> getSimList() {
        return simList;
    }

    public void setSimList(List<SimListInfo> simList) {
        this.simList = simList;
    }
}
