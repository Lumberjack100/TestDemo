package com.shmedo.mcloudapp.entity;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   ConfigParameterResult
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:31
 *
 */
public class ConfigParameterResult {

    private List<DataUploadInfo> dataUploadInfos;//数据上传方式集合，可能有多种上传方式
    private Integer dataCollectInterval; //数据采集间隔
    private Integer dataUploadInterval; //数据上传间隔
    private String configUpdateTime;//配置更新时间
    private String exValues;    //扩展配置
    private String note;        //备注


    public List<DataUploadInfo> getDataUploadInfos() {
        return dataUploadInfos;
    }


    public void setDataUploadInfos(List<DataUploadInfo> dataUploadInfos) {
        this.dataUploadInfos = dataUploadInfos;
    }


    public Integer getDataCollectInterval() {
        return dataCollectInterval;
    }


    public void setDataCollectInterval(Integer dataCollectInterval) {
        this.dataCollectInterval = dataCollectInterval;
    }


    public Integer getDataUploadInterval() {
        return dataUploadInterval;
    }


    public void setDataUploadInterval(Integer dataUploadInterval) {
        this.dataUploadInterval = dataUploadInterval;
    }


    public String getConfigUpdateTime() {
        return configUpdateTime;
    }


    public void setConfigUpdateTime(String configUpdateTime) {
        this.configUpdateTime = configUpdateTime;
    }


    public String getExValues() {
        return exValues;
    }


    public void setExValues(String exValues) {
        this.exValues = exValues;
    }


    public String getNote() {
        return note;
    }


    public void setNote(String note) {
        this.note = note;
    }
}
