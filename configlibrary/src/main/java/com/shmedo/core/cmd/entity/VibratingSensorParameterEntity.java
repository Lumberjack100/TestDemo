package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/28.
 * 设置振弦式传感器修正参数
 */
public class VibratingSensorParameterEntity implements Validater {
    private String channelNumber;//
    private String type;
    private String paramrter;

    public VibratingSensorParameterEntity(String channelNumber, String type, String paramrter) {
        this.channelNumber = channelNumber;
        this.type = type;
        this.paramrter = paramrter;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(channelNumber) && TextUtils.isEmpty(type) &&TextUtils.isEmpty(paramrter))
            throw new DASParameterException("参数不能为空");
    }

    @Override
    public String toString() {
        return channelNumber+""+type+""+paramrter;
    }
}
