package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DataUploadInfo
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:31
 *
 */
public class DataUploadInfo {
    private Integer DataUploadType;//数据上传方式Int值

    private String DataUploadTypeString;//数据上传方式String值

    private String DataUploadTarget;//数据上传目标


    public Integer getDataUploadType() {
        return DataUploadType;
    }


    public void setDataUploadType(Integer dataUploadType) {
        DataUploadType = dataUploadType;
    }


    public String getDataUploadTypeString() {
        return DataUploadTypeString;
    }


    public void setDataUploadTypeString(String dataUploadTypeString) {
        DataUploadTypeString = dataUploadTypeString;
    }


    public String getDataUploadTarget() {
        return DataUploadTarget;
    }


    public void setDataUploadTarget(String dataUploadTarget) {
        DataUploadTarget = dataUploadTarget;
    }
}
