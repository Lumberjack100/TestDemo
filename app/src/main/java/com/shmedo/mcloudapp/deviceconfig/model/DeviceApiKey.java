package com.shmedo.mcloudapp.deviceconfig.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/25/20 <br/>
 * 描述：     设备 ApiKey 实体
 */
@Entity
public class DeviceApiKey {
    @Unique
    private Integer deviceID;       //设备ID
    private String deviceToken;     //设备token
    private String apiKey;

    @Generated(hash = 257759885)
    public DeviceApiKey(Integer deviceID, String deviceToken, String apiKey) {
        this.deviceID = deviceID;
        this.deviceToken = deviceToken;
        this.apiKey = apiKey;
    }

    @Generated(hash = 164323198)
    public DeviceApiKey() {
    }

    public Integer getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
