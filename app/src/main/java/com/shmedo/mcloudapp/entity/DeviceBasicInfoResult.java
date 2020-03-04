package com.shmedo.mcloudapp.entity;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * 项目名：  mCloudapp<br/>
 * 包名：    com.shmedo.mcloudapp.entity<br/>
 * 文件名:   DeviceBasicInfoResult<br/>
 * 创建者:   dpc<br/>
 * 创建时间:  2019/8/8 18:39<br/>
 * 描述：    4.2 查询设备基础信息列表 地图展示<br/>
 */
@Entity
public class DeviceBasicInfoResult  implements Serializable {

    private static final long serialVersionUID = -4354247480033168035L;

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
    private Long id;    //设备编号
    private String deviceToken;//设备Token
    private String deviceName;//设备名称
    private int deviceTypeID;//设备类型ID
    private String deviceTypeName;//设备类型名称
    private String gpsLocation;//设备GPS位置
    private String installLocation;//设备安装位置说明
    private String securityNO;//设备解密后的安全码
    private boolean local;  //本地添加数据存储的标记
    private String account;     //用户账号



    @Generated(hash = 1369667473)
    public DeviceBasicInfoResult(Long id, String deviceToken, String deviceName, int deviceTypeID,
            String deviceTypeName, String gpsLocation, String installLocation, String securityNO,
            boolean local, String account) {
        this.id = id;
        this.deviceToken = deviceToken;
        this.deviceName = deviceName;
        this.deviceTypeID = deviceTypeID;
        this.deviceTypeName = deviceTypeName;
        this.gpsLocation = gpsLocation;
        this.installLocation = installLocation;
        this.securityNO = securityNO;
        this.local = local;
        this.account = account;
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

    public boolean getLocal() {
        return this.local;
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
    public String getAccount() {
        return this.account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

}
