package com.shmedo.mcloudapp.entity;

import com.google.gson.annotations.SerializedName;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DeviceBasicInfoResult
 * 创建者:   dpc
 * 创建时间:  2019/8/8 18:39
 * 描述：    4.2 查询设备基础信息列表 地图展示
 */
@Entity
public class DeviceBasicInfoResult {

    /**
     * deviceID : 72
     * deviceToken : 150009K
     * deviceName : 150009K
     * deviceTypeID : 5
     * deviceTypeName : DAG
     * gpsLocation : null
     * installLocation :
     * securityNO : 1.2345678E7
     */
    @Unique
    @Id(autoincrement = false)
    @SerializedName("deviceID")
    private Long id;    //设备编号deviceID

    private String deviceToken;
    private String deviceName;
    private int deviceTypeID;
    private String deviceTypeName;
    private String gpsLocation;
    private String installLocation;
    private String securityNO;
    private boolean local;  //本地添加数据存储的标记


    @Generated(hash = 754972352)
    public DeviceBasicInfoResult(Long id, String deviceToken, String deviceName, int deviceTypeID,
            String deviceTypeName, String gpsLocation, String installLocation, String securityNO,
            boolean local) {
        this.id = id;
        this.deviceToken = deviceToken;
        this.deviceName = deviceName;
        this.deviceTypeID = deviceTypeID;
        this.deviceTypeName = deviceTypeName;
        this.gpsLocation = gpsLocation;
        this.installLocation = installLocation;
        this.securityNO = securityNO;
        this.local = local;
    }
    @Generated(hash = 1939211013)
    public DeviceBasicInfoResult() {
    }


    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceToken() { return deviceToken;}


    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken;}


    public String getDeviceName() { return deviceName;}


    public void setDeviceName(String deviceName) { this.deviceName = deviceName;}


    public int getDeviceTypeID() { return deviceTypeID;}


    public void setDeviceTypeID(int deviceTypeID) { this.deviceTypeID = deviceTypeID;}


    public String getDeviceTypeName() { return deviceTypeName;}


    public void setDeviceTypeName(String deviceTypeName) { this.deviceTypeName = deviceTypeName;}


    public String getGpsLocation() { return gpsLocation;}


    public void setGpsLocation(String gpsLocation) { this.gpsLocation = gpsLocation;}


    public String getInstallLocation() { return installLocation;}


    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }


    public String getSecurityNO() { return securityNO;}


    public void setSecurityNO(String securityNO) { this.securityNO = securityNO;}


    public boolean isLocal() {
        return local;
    }


    public void setLocal(boolean local) {
        this.local = local;
    }


    @Override public String toString() {
        return "DeviceBasicInfoResult{" +
            "id=" + id +
            ", deviceToken='" + deviceToken + '\'' +
            ", deviceName='" + deviceName + '\'' +
            ", deviceTypeID=" + deviceTypeID +
            ", deviceTypeName='" + deviceTypeName + '\'' +
            ", gpsLocation='" + gpsLocation + '\'' +
            ", installLocation='" + installLocation + '\'' +
            ", securityNO='" + securityNO + '\'' +
            ", local=" + local +
            '}';
    }
    public boolean getLocal() {
        return this.local;
    }
}
