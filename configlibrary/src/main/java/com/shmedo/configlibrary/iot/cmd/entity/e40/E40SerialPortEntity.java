package com.shmedo.configlibrary.iot.cmd.entity.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/18 <br/>
 * 描述：     生成 串口参数拼接指令
 */
public class E40SerialPortEntity implements Validater {
    private String type;//输出数据格式
    private String baud;//波特率
    private String databits;//
    private String parity;
    private String stopbits;

    public void setType(String type) {
        this.type = type;
    }

    public void setBaud(String baud) {
        this.baud = baud;
    }

    public void setDatabits(String databits) {
        this.databits = databits;
    }

    public void setParity(String parity) {
        this.parity = parity;
    }

    public void setStopbits(String stopbits) {
        this.stopbits = stopbits;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type=" + type);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(baud)) {
            stringBuilder.append("baud=" + baud);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(databits)) {
            stringBuilder.append("databits=" + databits);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(parity)) {
            stringBuilder.append("parity=" + parity);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(stopbits)) {
            stringBuilder.append("stopbits=" + stopbits);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
