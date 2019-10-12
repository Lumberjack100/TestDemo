package com.shmedo.mcloudapp.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   SystemDataInfo
 * 创建者:   dpc
 * 创建时间:  2019/4/15 10:23
 * 描述：    系统列表实体类
 */
@Entity
public class SystemDataInfo implements Serializable {

    private static final long serialVersionUID = -3988482936599961689L;
    
    @Unique
    private int projID;
    private String projName;
    private String centerPoint;
    private String account;     //用户账号


    @Generated(hash = 1442683523)
    public SystemDataInfo(int projID, String projName, String centerPoint,
            String account) {
        this.projID = projID;
        this.projName = projName;
        this.centerPoint = centerPoint;
        this.account = account;
    }


    @Generated(hash = 786892869)
    public SystemDataInfo() {
    }


    public int getProID() {
        return projID;
    }


    public void setProID(int projID) {
        this.projID = projID;
    }


    public String getProjName() {
        return projName;
    }


    public void setProjName(String projName) {
        this.projName = projName;
    }


    public String getCenterPoint() {
        return centerPoint;
    }


    public void setCenterPoint(String centerPoint) {
        this.centerPoint = centerPoint;
    }


    public int getProjID() {
        return this.projID;
    }


    public void setProjID(int projID) {
        this.projID = projID;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "SystemDataInfo{" +
                "projID=" + projID +
                ", projName='" + projName + '\'' +
                ", centerPoint='" + centerPoint + '\'' +
                '}';
    }
}
