package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/12 <br/>
 * 描述：    Vms网关的基本信息
 */
public class VmsBasicInfo {
    private String sn;
    private String swVersion;//固件版本号
    private String volt;//供电电压
    private String online;//在线状态（0：离线，非0:在线)

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getSwVersion() {
        return TextUtils.isEmpty(swVersion) ? "" : swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getVolt() {
        return TextUtils.isEmpty(volt) ? "" : volt;
    }

    public void setVolt(String volt) {
        this.volt = volt;
    }

    public String getOnline() {
        return TextUtils.isEmpty(online) ? "" : online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
