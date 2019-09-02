package com.shmedo.mcloudapp.entity;

import java.io.Serializable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   SystemDataInfo
 * 创建者:   dpc
 * 创建时间:  2019/4/15 10:23
 * 描述：    系统列表实体类
 */
public class SystemDataInfo implements Serializable {
    private int projID;
    private String projName;
    private String centerPoint;


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
}
