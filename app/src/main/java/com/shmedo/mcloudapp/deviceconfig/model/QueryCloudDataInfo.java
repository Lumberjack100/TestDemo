package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 创建者:   dpc
 * 创建时间:  2019-12-16
 * 描述：    查询设备返回实体类
 */
public class QueryCloudDataInfo {
    private String timeStr;
    private String dataType;
    private String content;

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
