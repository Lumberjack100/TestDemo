package com.shmedo.mcloudapp.entity;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DeviceDetailInfo
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:30
 * 描述：    TODO
 */
public class DeviceDetailInfo {
    private BasicInfoResult basicInfo;      //基础信息
    private List<DeviceSensorResult> sensors;   //传感器信息
    private DetailInfoResult detailInfo;        //详细配置信息
    private  ConfigParameterResult configParameter; //配置参数


    public BasicInfoResult getBasicInfo() {
        return basicInfo;
    }


    public void setBasicInfo(BasicInfoResult basicInfo) {
        this.basicInfo = basicInfo;
    }


    public List<DeviceSensorResult> getSensors() {
        return sensors;
    }


    public void setSensors(List<DeviceSensorResult> sensors) {
        this.sensors = sensors;
    }


    public DetailInfoResult getDetailInfo() {
        return detailInfo;
    }


    public void setDetailInfo(DetailInfoResult detailInfo) {
        this.detailInfo = detailInfo;
    }


    public ConfigParameterResult getConfigParameter() {
        return configParameter;
    }


    public void setConfigParameter(ConfigParameterResult configParameter) {
        this.configParameter = configParameter;
    }
}
