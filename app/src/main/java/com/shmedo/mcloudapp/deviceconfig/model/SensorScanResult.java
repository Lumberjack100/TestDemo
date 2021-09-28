package com.shmedo.mcloudapp.deviceconfig.model;

import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/28 <br/>
 * 描述：     振弦式传感器扫码配置参数
 */
public class SensorScanResult {

    private String productor;
    private String sensortype;
    private String serealNum;
    private LinkedTreeMap<String, String> param;
//    private LinearSensorParam param;

    public String getProductor() {
        return productor;
    }

    public void setProductor(String productor) {
        this.productor = productor;
    }

    public String getSensortype() {
        return TextUtils.isEmpty(sensortype) ? "" : sensortype;
    }

    public void setSensortype(String sensortype) {
        this.sensortype = sensortype;
    }

    public String getSerealNum() {
        return serealNum;
    }

    public void setSerealNum(String serealNum) {
        this.serealNum = serealNum;
    }

    public LinkedTreeMap<String, String> getParam() {
        return param;
    }

    public void setParam(LinkedTreeMap<String, String> param) {
        this.param = param;
    }

//    public LinearSensorParam getParam() {
//        return param;
//    }
//
//    public void setParam(LinearSensorParam param) {
//        this.param = param;
//    }
}
