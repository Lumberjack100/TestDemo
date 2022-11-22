package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/18 <br/>
 * 描述：      E40 串口参数信息
 */
public class E40SerialPortInfo {
    private String type;//输出数据格式
    private String baud;//波特率
    private String databits;//
    private String parity;
    private String stopbits;

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBaud() {
        return TextUtils.isEmpty(baud) ? "" : baud;
    }

    public void setBaud(String baud) {
        this.baud = baud;
    }

    public String getDatabits() {
        return TextUtils.isEmpty(databits) ? "" : databits;
    }

    public void setDatabits(String databits) {
        this.databits = databits;
    }

    public String getParity() {
        return TextUtils.isEmpty(parity) ? "" : parity;
    }

    public void setParity(String parity) {
        this.parity = parity;
    }

    public String getStopbits() {
        return TextUtils.isEmpty(stopbits) ? "" : stopbits;
    }

    public void setStopbits(String stopbits) {
        this.stopbits = stopbits;
    }
}
