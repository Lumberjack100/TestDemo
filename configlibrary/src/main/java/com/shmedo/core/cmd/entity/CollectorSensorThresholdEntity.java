package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器接入传感器触发阈值参数
 */
public class CollectorSensorThresholdEntity implements Validater {
    private String type;
    private String threshold;

    public CollectorSensorThresholdEntity(String type, String threshold) {
        this.type = type;
        this.threshold = threshold;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(type) && TextUtils.isEmpty(threshold))
            throw new DASParameterException("不能为空");

        if(!CollectorModel.isValidCollector(this.type))
            throw new DASParameterException("采集器不存在");
    }

    @Override
    public String toString() {
        return type+""+threshold;
    }
}
