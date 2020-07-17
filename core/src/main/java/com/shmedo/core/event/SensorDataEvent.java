package com.shmedo.core.event;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.event
 * 文件名:   SensorDataEvent
 * 创建者:   dpc
 * 创建时间:  2019/6/19 14:07
 * 描述：    传感器事件类
 */
public class SensorDataEvent {
    private String message;
    private String type;


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }
}
