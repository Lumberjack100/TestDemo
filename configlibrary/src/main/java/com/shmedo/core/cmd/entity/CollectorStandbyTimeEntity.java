package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by adu on 2017/12/19.
 * 设置采集器待机时长参数
 */
public class CollectorStandbyTimeEntity implements Validater {
    private String type;
    private String time;

    public CollectorStandbyTimeEntity(String type, String time) {
        this.type = type;
        this.time = time;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(type) && TextUtils.isEmpty(time))
            throw new DASParameterException("不能为空");

        if(!CollectorModel.isValidCollector(this.type))
            throw new DASParameterException("采集器不存在");

    }

    @Override
    public String toString() {
        return type+""+time;
    }
}
