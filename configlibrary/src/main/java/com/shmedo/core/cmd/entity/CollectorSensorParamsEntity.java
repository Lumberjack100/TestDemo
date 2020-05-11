package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/14.
 * 获取XX采集器YY传感器通道的传感器参数
 */
public class CollectorSensorParamsEntity implements Validater {

    private String collectorType;   //采集器类型
    private String channelNumber;   //通道号

    public CollectorSensorParamsEntity(String collectorType, String channelNumber) {
        this.collectorType = collectorType;
        this.channelNumber = channelNumber;
    }

    @Override
    public void validate() {
        if (!CollectorModel.isValidCollector(this.collectorType))
            throw new DASParameterException("采集器不存在");

        if (TextUtils.isEmpty(this.channelNumber))
            throw new DASParameterException("通道号不能为空");
    }

    @Override
    public String toString() {
        return this.collectorType + "" + this.channelNumber;
    }
}
