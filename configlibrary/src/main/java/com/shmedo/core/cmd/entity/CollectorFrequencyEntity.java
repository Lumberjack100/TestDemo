package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器采集频度参数
 */
public class CollectorFrequencyEntity implements Validater {
    private String type;
    private String timeInterval;

    public CollectorFrequencyEntity(String type, String timeInterval) {
        this.type = type;
        this.timeInterval = timeInterval;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(type) && TextUtils.isEmpty(timeInterval))
            throw new DASParameterException("不能为空");
        if(!CollectorModel.isValidCollector(this.type))
            throw new DASParameterException("采集器不存在");
    }

    @Override
    public String toString() {
        return type+""+timeInterval;
    }
}
