package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/2 <br/>
 * 描述：     E40基站位置信息
 */
public class E40BasePositionInfo {
    private String mode;//1表示自动模式，2表示第一次自动获取以后采用第一次值，3表示手动模式，当为自动模式时，可不设置其他参数。
    private String lon;//经度
    private String lat;//纬度
    private String alt;//高程

    public String getMode() {
        return TextUtils.isEmpty(mode) ? "1" : mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLon() {
        return TextUtils.isEmpty(lon) ? "" : lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return TextUtils.isEmpty(lat) ? "" : lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getAlt() {
        return TextUtils.isEmpty(alt) ? "" : alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }
}
