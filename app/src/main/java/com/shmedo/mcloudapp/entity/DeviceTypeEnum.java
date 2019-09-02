package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DeviceTypeEnum
 * 创建者:   dpc
 * 创建时间:  2019/1/28 16:32
 * 描述：    TODO
 */
public enum DeviceTypeEnum {
    DAS("DAS"),
    DAG("DAG"),
    E60("E60"),
    PVS("PVS");

    private String type;


    DeviceTypeEnum(String type) {
        this.type = type;
    }


    @Override public String toString() {
        return this.type;
    }
    public static boolean  value(String type) {
        switch (type){
            case "DAS": return true;
            case "DAG": return true;
            case "E60": return true;
            case "PVS": return true;
            default:    return false;
        }
    }
}
