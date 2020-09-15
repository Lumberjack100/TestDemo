package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.DataCommunicateMode;

/**
 * Created by adu on 2017/12/15.
 * 数据通讯模式实体类
 */
public class DataCommunicateModeInfo {

    private DataCommunicateMode dataCommunicateMode;

    public DataCommunicateMode getDataCommunicateMode() {
        return dataCommunicateMode;
    }

    public void setDataCommunicateMode(DataCommunicateMode dataCommunicateMode) {
        this.dataCommunicateMode = dataCommunicateMode;
    }

    @Override
    public String toString() {
        return "DataCommunicateModeInfo{" +
                "dataCommunicateMode=" + dataCommunicateMode +
                '}';
    }
}
