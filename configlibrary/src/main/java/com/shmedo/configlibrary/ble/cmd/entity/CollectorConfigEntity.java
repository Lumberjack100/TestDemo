package com.shmedo.configlibrary.ble.cmd.entity;


import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

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
