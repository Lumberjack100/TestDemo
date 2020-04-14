package com.shmedo.core.model;


import com.shmedo.core.enums.DataMessageModel;

/**
 * Created by adu on 2017/12/15.
 * 数据通讯模式实体类
 */
public class DataMessageModelInfo {

    private DataMessageModel dataMessageModel;

    public DataMessageModel getDataMessageModel() {
        return dataMessageModel;
    }

    public void setDataMessageModel(DataMessageModel dataMessageModel) {
        this.dataMessageModel = dataMessageModel;
    }

    @Override
    public String toString() {
        return "DataMessageModelInfo{" +
                "dataMessageModel=" + dataMessageModel +
                '}';
    }
}
