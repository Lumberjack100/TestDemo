package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/21.
 * 设置采集器接入传感器修正值（只有墒情计用到3个修正值，其他传感器只用到一个修正值）
 */
public class CollectorSensorRevisedEntity implements Validater {
    private String collectorModel;
    private String address;
    private String humidity;
    private String salinity;
    private String temperature;

    public CollectorSensorRevisedEntity(String collectorModel, String address, String humidity, String salinity, String temperature) {
        this.collectorModel = collectorModel;
        this.address = address;
        this.humidity = humidity;
        this.salinity = salinity;
        this.temperature = temperature;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(collectorModel) && TextUtils.isEmpty(address)&& TextUtils.isEmpty(humidity)
                && TextUtils.isEmpty(salinity)&& TextUtils.isEmpty(temperature))
            throw new DASParameterException("不能为空");
        if(!CollectorModel.isValidCollector(this.collectorModel))
            throw new DASParameterException("采集器不存在");
    }

    @Override
    public String toString() {
        return collectorModel+""+address+""+humidity+""+salinity+""+temperature;
    }
}
