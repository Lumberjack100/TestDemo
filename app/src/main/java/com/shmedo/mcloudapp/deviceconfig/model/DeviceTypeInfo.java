package com.shmedo.mcloudapp.deviceconfig.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/15 <br/>
 * 描述：    设备类型实体
 */
@Entity
public class DeviceTypeInfo {

    /**
     * id : 4
     * deviceTypeToken : DAS
     * deviceTypeName : DAS
     * displayOrder : 0
     * desc : 低功耗数据采集仪
     * createUserID : 1
     * createTime : 2018-01-23 16:06:08
     * updateUserID : 1
     * updateTime : 2018-01-23 16:06:08
     */

    @Unique
    private int id; //在 sqllite 中作为主键
    private String deviceTypeToken;
    private String deviceTypeName;
    private int displayOrder;
    private String desc;
    private int createUserID;
    private String createTime;
    private int updateUserID;
    private String updateTime;

    @Generated(hash = 1527164791)
    public DeviceTypeInfo(int id, String deviceTypeToken, String deviceTypeName,
            int displayOrder, String desc, int createUserID, String createTime,
            int updateUserID, String updateTime) {
        this.id = id;
        this.deviceTypeToken = deviceTypeToken;
        this.deviceTypeName = deviceTypeName;
        this.displayOrder = displayOrder;
        this.desc = desc;
        this.createUserID = createUserID;
        this.createTime = createTime;
        this.updateUserID = updateUserID;
        this.updateTime = updateTime;
    }

    @Generated(hash = 1993526036)
    public DeviceTypeInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceTypeToken() {
        return deviceTypeToken;
    }

    public void setDeviceTypeToken(String deviceTypeToken) {
        this.deviceTypeToken = deviceTypeToken;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCreateUserID() {
        return createUserID;
    }

    public void setCreateUserID(int createUserID) {
        this.createUserID = createUserID;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getUpdateUserID() {
        return updateUserID;
    }

    public void setUpdateUserID(int updateUserID) {
        this.updateUserID = updateUserID;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
