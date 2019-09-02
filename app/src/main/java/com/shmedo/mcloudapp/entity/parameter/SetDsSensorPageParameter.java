package com.shmedo.mcloudapp.entity.parameter;

import com.shmedo.mcloudapp.util.page.ParameterValidate;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   SetDsSensorPageParameter
 * 创建者:   dpc
 * 创建时间:  2019/6/27 14:24
 * 描述：    TODO
 */
public class SetDsSensorPageParameter implements ParameterValidate {
    private String sensorAddress;
    private String correctionValue;
    private String triggerThreshold;


    public String getSensorAddress() {
        return sensorAddress;
    }

    public void setSensorAddress(String sensorAddress) {
        this.sensorAddress = sensorAddress;
    }

    public String getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    @Override public void validate() {

    }
}
