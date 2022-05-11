package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：     指令日志信息
 */
public class CommonLogInfo {
    private String logTime;
    private String logContent;
    private int color;

    public CommonLogInfo(String logTime, String logContent) {
        this.logTime = logTime;
        this.logContent = logContent;
    }

    public CommonLogInfo(String logTime, String logContent, int color) {
        this.logTime = logTime;
        this.logContent = logContent;
        this.color = color;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public int getColor() {
        return color;
    }
}
