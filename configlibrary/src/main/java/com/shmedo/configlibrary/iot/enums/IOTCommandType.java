package com.shmedo.configlibrary.iot.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：    米度物联网设备指令
 */
public enum IOTCommandType {
    /**
     * 获取设备终端时间
     */
    QUERY_TERMINAL_TIME("reqtime"),

    /**
     * 设置设备终端时间
     */
    SET_TERMINAL_TIME("settime"),

    /**
     * 获取设备状态
     */
    QUERY_DEVICE_STATUS("getstatus"),

    /**
     * 重启设备
     */
    REBOOT("reboot"),

    /**
     * 获取接入传感器类型
     */
    QUERY_SENSOR_TYPE("getsensorID"),

    /**
     * 传感器遥测
     */
    QUERY_SAMPLE("sample"),

    /**
     * 设置工作模式
     */
    SET_WORK_MODE("setworkmode"),



    /**
     * 批处理结束指令
     */
    BATCH_END("");


    private String commandCode;

    IOTCommandType(String commandCode) {
        this.commandCode = commandCode;
    }

    @Override
    public String toString() {
        return this.commandCode;
    }
}
