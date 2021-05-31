package com.shmedo.configlibrary.iot.cmd.entity.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：     生成DAS 北斗数传终端参数拼接指令
 */
public class DasBdTerminalEntity implements Validater {
    private String sw;//0 关闭 1 开启
    private String dstaddr;//目标地址
    private String baud;//波特率

    public void setSw(String sw) {
        this.sw = sw;
    }

    public void setDstaddr(String dstaddr) {
        this.dstaddr = dstaddr;
    }

    public void setBaud(String baud) {
        this.baud = baud;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sw=" + sw);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(dstaddr)) {
            stringBuilder.append("dstaddr=" + dstaddr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(baud)) {
            stringBuilder.append("baud=" + baud);
            stringBuilder.append("&");
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
