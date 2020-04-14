package com.shmedo.core.cmd.entity;


import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2017/12/12.
 * 采集器配置
 */
public class CollectorConfigEntity implements Validater {
    private String collectorType;

    public CollectorConfigEntity(String collectorType) {
        this.collectorType = collectorType;
    }

    @Override
    public void validate() {
        if(!CollectorModel.isValidCollector(this.collectorType))
            throw new DASParameterException("采集器不存在");
    }

    @Override
    public String toString() {
        return this.collectorType;
    }
}
