package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：     北斗数传终端
 */
public class DasBdTerminalInfo {
    private String sw;//0 关闭 1 开启
    private String dstaddr;//目标地址
    private String baud;//波特率

    public String getSw() {
        return TextUtils.isEmpty(sw) ? "" : sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public String getDstaddr() {
        return TextUtils.isEmpty(dstaddr) ? "" : dstaddr;
    }

    public void setDstaddr(String dstaddr) {
        this.dstaddr = dstaddr;
    }

    public String getBaud() {
        return TextUtils.isEmpty(baud) ? "" : baud;
    }

    public void setBaud(String baud) {
        this.baud = baud;
    }
}
