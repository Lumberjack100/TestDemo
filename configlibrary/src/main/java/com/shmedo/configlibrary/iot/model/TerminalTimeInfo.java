package com.shmedo.configlibrary.iot.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：    设备终端时间实体类
 */
public class TerminalTimeInfo {
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.time;
    }
}
